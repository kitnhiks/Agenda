package hiks.client.model;

import java.io.Serializable;

public class TaskPriority implements Serializable {

	private static final long serialVersionUID = 1L;

	private String symbol;
	private double priority;
	private double change;

	public TaskPriority() {
	}

	public TaskPriority(String symbol, double priority, double change) {
		this.symbol = symbol;
		this.priority = priority;
		this.change = change;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public double getPriority() {
		return this.priority;
	}

	public double getChange() {
		return this.change;
	}

	public double getChangePercent() {
		return 100.0 * this.change / this.priority;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setPriority(double priority) {
		this.priority = priority;
	}

	public void setChange(double change) {
		this.change = change;
	}
}