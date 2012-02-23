package hiks.server.service;

import hiks.client.exception.NotLoggedInException;
import hiks.client.service.TaskService;
import hiks.server.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TaskServiceImpl extends RemoteServiceServlet implements
TaskService {
	private static final Logger LOG = Logger.getLogger(TaskServiceImpl.class.getName());
	  private static final PersistenceManagerFactory PMF =
	      JDOHelper.getPersistenceManagerFactory("transactions-optional");

	  public void addStock(String symbol) throws NotLoggedInException {
	    checkLoggedIn();
	    PersistenceManager pm = getPersistenceManager();
	    try {
	      pm.makePersistent(new Task(getUser(), symbol));
	    } finally {
	      pm.close();
	    }
	  }

	  public void removeStock(String symbol) throws NotLoggedInException {
	    checkLoggedIn();
	    PersistenceManager pm = getPersistenceManager();
	    try {
	      long deleteCount = 0;
	      Query q = pm.newQuery(Task.class, "user == u");
	      q.declareParameters("com.google.appengine.api.users.User u");
	      List<Task> stocks = (List<Task>) q.execute(getUser());
	      for (Task stock : stocks) {
	        if (symbol.equals(stock.getSymbol())) {
	          deleteCount++;
	          pm.deletePersistent(stock);
	        }
	      }
	      if (deleteCount != 1) {
	        LOG.log(Level.WARNING, "removeStock deleted "+deleteCount+" Stocks");
	      }
	    } finally {
	      pm.close();
	    }
	  }

	  public String[] getStocks() throws NotLoggedInException {
	    checkLoggedIn();
	    PersistenceManager pm = getPersistenceManager();
	    List<String> symbols = new ArrayList<String>();
	    try {
	      Query q = pm.newQuery(Task.class, "user == u");
	      q.declareParameters("com.google.appengine.api.users.User u");
	      q.setOrdering("createDate");
	      List<Task> stocks = (List<Task>) q.execute(getUser());
	      for (Task stock : stocks) {
	        symbols.add(stock.getSymbol());
	      }
	    } finally {
	      pm.close();
	    }
	    return (String[]) symbols.toArray(new String[0]);
	  }

	  private void checkLoggedIn() throws NotLoggedInException {
	    if (getUser() == null) {
	      throw new NotLoggedInException("Not logged in.");
	    }
	  }

	  private User getUser() {
	    UserService userService = UserServiceFactory.getUserService();
	    return userService.getCurrentUser();
	  }

	  private PersistenceManager getPersistenceManager() {
	    return PMF.getPersistenceManager();
	  }
	}
