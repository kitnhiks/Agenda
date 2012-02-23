package hiks.client;

import java.util.ArrayList;
import java.util.Date;

import hiks.client.exception.DelistedException;
import hiks.client.exception.NotLoggedInException;
import hiks.client.model.LoginInfo;
import hiks.client.model.TaskPriority;
import hiks.client.service.LoginService;
import hiks.client.service.LoginServiceAsync;
import hiks.client.service.TaskPriorityService;
import hiks.client.service.TaskPriorityServiceAsync;
import hiks.client.service.TaskService;
import hiks.client.service.TaskServiceAsync;
import hiks.shared.FieldVerifier;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Agenda implements EntryPoint {

	private static final int REFRESH_INTERVAL = 5000; // ms

	/** Task Table **/
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable tasksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newTaskTextBox = new TextBox();
	private Button addTaskButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<String> tasks = new ArrayList<String>();
	private TaskPriorityServiceAsync taskPrioritySvc = (TaskPriorityServiceAsync) GWT.create(TaskPriorityService.class);
	private Label errorMsgLabel = new Label();

	/** LOGIN **/
	private LoginInfo loginInfo = null;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label("Please sign in to your Google Account to access the StockWatcher application.");
	private Anchor signInLink = new Anchor("Sign In");
	private Anchor signOutLink = new Anchor("Sign Out");

	/** JDO **/
	private final TaskServiceAsync stockService = GWT.create(TaskService.class);

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		// Check login status using login service.
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}

			public void onSuccess(LoginInfo result) {
				loginInfo = result;
				if(loginInfo.isLoggedIn()) {
					loadStockWatcher();
				} else {
					loadLogin();
				}
			}
		});
	}

	private void loadLogin() {
		// Assemble login panel.
		signInLink.setHref(loginInfo.getLoginUrl());
		loginPanel.add(loginLabel);
		loginPanel.add(signInLink);
		RootPanel.get("tasksList").add(loginPanel);
	}


	private void loadStockWatcher() {
		// Set up sign out hyperlink.
		signOutLink.setHref(loginInfo.getLogoutUrl());

		// Create table for tasks.
		tasksFlexTable.setText(0, 0, "Tâche");
		tasksFlexTable.setText(0, 1, "DeadLine");
		tasksFlexTable.setText(0, 2, "Priorité");
		tasksFlexTable.setText(0, 3, "Remove");

		// Add styles to elements in the stock list table.
		tasksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
		tasksFlexTable.addStyleName("watchList");
		tasksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
		tasksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
		tasksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");

		loadStocks();

		// Assemble Add Task panel.
		addPanel.add(newTaskTextBox);
		addPanel.add(addTaskButton);
		addPanel.addStyleName("addPanel");

		// Assemble Main panel.
		errorMsgLabel.setStyleName("errorMessage");
		errorMsgLabel.setVisible(false);

		mainPanel.add(errorMsgLabel);
		mainPanel.add(signOutLink);
		mainPanel.add(tasksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("tasksList").add(mainPanel);

		// Move cursor focus to the input box.
		newTaskTextBox.setFocus(true);

		// Setup timer to refresh list automatically.
		Timer refreshTimer = new Timer() {
			@Override
			public void run() {
				refreshWatchList();
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

		// Listen for mouse events on the Add button.
		addTaskButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addTask();
			}
		});

		// Listen for keyboard events in the input box.
		newTaskTextBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					addTask();
				}
			}
		});
	}

	private void loadStocks() {
		stockService.getStocks(new AsyncCallback<String[]>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(String[] symbols) {
				displayStocks(symbols);
			}
		});
	}

	private void displayStocks(String[] symbols) {
		for (String symbol : symbols) {
			displayStock(symbol);
		}
	}

	/**
	 * Ajoute une tache dans la table des tâches. 
	 * Déclenchée par un click sur le addTaskButton ou par la touche Enter depuis le champ newTaskTextBox.
	 */
	private void addTask() {
		String symbol = newTaskTextBox.getText().trim();
		newTaskTextBox.setFocus(true);

		// Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
		if (symbol.length()<3) {
			Window.alert("le nom de la tâche doit faire plus de 3 caractères.");
			newTaskTextBox.selectAll();
			return;
		}

		newTaskTextBox.setText("");

		// Rename the task if it's already in the table.
		if (tasks.contains(symbol)){
			int n=1;
			while (tasks.contains(symbol +" ("+n+")")){
				n++;
			}
			symbol = symbol +" ("+n+")";
		}

		final String finalSymbol = symbol;
		addStock(symbol);
	}

	private void addStock(final String symbol) {
		stockService.addStock(symbol, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Void ignore) {
				displayStock(symbol);
			}
		});
	}

	private void displayStock(final String finalSymbol) {
		// Add the task to the table.
		int row = tasksFlexTable.getRowCount();
		tasks.add(finalSymbol);
		tasksFlexTable.setText(row, 0, finalSymbol);
		tasksFlexTable.setWidget(row, 2, new Label());
		tasksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
		tasksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
		tasksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");

		// Add a button to remove this task from the table.
		Button removeTaskButton = new Button("x");
		removeTaskButton.addStyleDependentName("remove");
		removeTaskButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				removeStock(finalSymbol);
			}
		});
		tasksFlexTable.setWidget(row, 3, removeTaskButton);

		// Get the stock price.
		refreshWatchList();

		newTaskTextBox.setFocus(true);
	}

	private void removeStock(final String symbol) {
		stockService.removeStock(symbol, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}
			public void onSuccess(Void ignore) {
				undisplayStock(symbol);
			}
		});
	}

	private void undisplayStock(String symbol) {
		int removedIndex = tasks.indexOf(symbol);
		tasks.remove(removedIndex);
		tasksFlexTable.removeRow(removedIndex + 1);
	}
	
	private void refreshWatchList() {

		// Initialize the service proxy.
		if (taskPrioritySvc == null) {
			taskPrioritySvc = (TaskPriorityServiceAsync)GWT.create(TaskPriorityService.class);
		}

		ServiceDefTarget serviceDef = (ServiceDefTarget) taskPrioritySvc;
		serviceDef.setServiceEntryPoint(GWT.getModuleBaseURL() + "tasksPriority");

		// Set up the callback object.
		AsyncCallback<TaskPriority[]> callback = new AsyncCallback<TaskPriority[]>() {
			public void onFailure(Throwable caught) {
				// If the stock code is in the list of delisted codes, display an error message.
				String details = caught.getMessage();
				if (caught instanceof DelistedException) {
					details = "Company '" + ((DelistedException)caught).getSymbol() + "' was delisted";
				}

				errorMsgLabel.setText("Error: " + details);
				errorMsgLabel.setVisible(true);
			}

			public void onSuccess(TaskPriority[] result) {
				updateTable(result);
			}
		};

		// Make the call to the stock price service.
		taskPrioritySvc.getPriority(tasks.toArray(new String[0]), callback);

	}

	private void updateTable(TaskPriority[] priority) {
		for (int i = 0; i < priority.length; i++) {
			updateTable(priority[i]);
		}

		// Display timestamp showing last refresh.
		lastUpdatedLabel.setText("Last update : "
				+ DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM)
				.format(new Date()));

		// Clear any errors.
		errorMsgLabel.setVisible(false);
	}

	private void updateTable(TaskPriority priority) {
		// Make sure the stock is still in the stock table.
		if (!tasks.contains(priority.getSymbol())) {
			return;
		}

		int row = tasks.indexOf(priority.getSymbol()) + 1;

		// Format the data in the Price and Change fields.
		String priorityText = NumberFormat.getFormat("#,##0.00").format(priority.getPriority());
		NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		String changeText = changeFormat.format(priority.getChange());
		String changePercentText = changeFormat.format(priority.getChangePercent());

		// Populate the Price and Change fields with new data.
		tasksFlexTable.setText(row, 1, priorityText);
		Label changeWidget = (Label)tasksFlexTable.getWidget(row, 2);
		changeWidget.setText(changeText + " (" + changePercentText + "%)");

		// Change the color of text in the Change field based on its value.
		String changeStyleName = "noChange";
		if (priority.getChangePercent() < -0.1f) {
			changeStyleName = "negativeChange";
		}
		else if (priority.getChangePercent() > 0.1f) {
			changeStyleName = "positiveChange";
		}

		changeWidget.setStyleName(changeStyleName);
	}
	 
	 private void handleError(Throwable error) {
		    Window.alert(error.getMessage());
		    if (error instanceof NotLoggedInException) {
		      Window.Location.replace(loginInfo.getLogoutUrl());
		    }
		  }
}
