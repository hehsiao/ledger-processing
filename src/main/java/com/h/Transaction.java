package com.h;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Transaction implements Comparable<Transaction> {
	private Calendar date;
	private String ledger;
	private BigDecimal amount;
	private String company;
	private final SimpleDateFormat dateParseFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private final SimpleDateFormat dateDisplayFormat = new SimpleDateFormat(
			"MMM dd, yyyy");
	private final NumberFormat currencyFormatter = NumberFormat
			.getCurrencyInstance();

	public Calendar getDate() {
		return date;
	}

	public void setDate(String dateStr) {
		try {
			Calendar date = new GregorianCalendar();
			date.setTime(dateParseFormat.parse(dateStr));
			this.date = date;
		} catch (ParseException e) {
			System.err.println("Unable to parse date [" + dateStr + "]");
		}
	}

	public String getLedger() {
		return ledger;
	}

	public void setLedger(String ledger) {
		this.ledger = ledger;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public int compareTo(Transaction o) {
		return getDate().compareTo(o.getDate());
	}

	public String printTable(String format) {
		return String.format(format, dateDisplayFormat.format(date.getTime()),
				company, currencyFormatter.format(amount));
	}

	@Override
	public String toString() {
		return "[date=" + dateDisplayFormat.format(date.getTime())
				+ ", ledger=" + ledger + ", amount=" + amount + ", company="
				+ company + "]";
	}
}
