package hiks.client.service;
import hiks.client.exception.NotLoggedInException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("task")
public interface TaskService extends RemoteService{
	public void addStock(String symbol) throws NotLoggedInException;
	public void removeStock(String symbol) throws NotLoggedInException;
	public String[] getStocks() throws NotLoggedInException;
}

