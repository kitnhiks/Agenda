package hiks.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TaskServiceAsync {
	public void addStock(String symbol, AsyncCallback<Void> async);
	public void removeStock(String symbol, AsyncCallback<Void> async);
	public void getStocks(AsyncCallback<String[]> async);
}