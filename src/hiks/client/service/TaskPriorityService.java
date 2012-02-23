package hiks.client.service;

import hiks.client.exception.DelistedException;
import hiks.client.model.TaskPriority;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("tasksPriority")
public interface TaskPriorityService extends RemoteService{
	TaskPriority[] getPriority(String[] symbols) throws DelistedException;
}
