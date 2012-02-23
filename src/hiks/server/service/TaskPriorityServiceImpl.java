package hiks.server.service;


import hiks.client.exception.DelistedException;
import hiks.client.model.TaskPriority;
import hiks.client.service.TaskPriorityService;

import java.util.Random;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TaskPriorityServiceImpl extends RemoteServiceServlet implements TaskPriorityService{

	private static final long serialVersionUID = 1L;

	private static final double MAX_PRICE = 100.0; // $100.00
	private static final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

	public TaskPriority[] getPriority(String[] symbols) throws DelistedException{
		Random rnd = new Random();

		TaskPriority[] priorities = new TaskPriority[symbols.length];
		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i].equals("ERR")) {
				throw new DelistedException("ERR");
			}

			double priority = rnd.nextDouble() * MAX_PRICE;
			double change = priority * MAX_PRICE_CHANGE * (rnd.nextDouble() * 2f - 1f);

			priorities[i] = new TaskPriority(symbols[i], priority, change);
		}

		return priorities;
	}

}
