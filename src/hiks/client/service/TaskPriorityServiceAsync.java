package hiks.client.service;

import hiks.client.model.TaskPriority;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TaskPriorityServiceAsync {
	void getPriority(String[] symbols, AsyncCallback<TaskPriority[]> callback);
}
