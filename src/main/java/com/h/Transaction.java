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
	private final SimpleDateFormat dateformat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private final SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
	private final NumberFormat currencyFormatter = NumberFormat
			.getCurrencyInstance();

	public Calendar getDate() {
		return date;
	}

	public void setDate(String dateStr) {
		try {
			Calendar date = new GregorianCalendar();
			date.setTime(dateformat.parse(dateStr));
			this.date = date;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	@Override
	public String toString() {
		return String.format("%-15s%-50s%10s", df.format(date.getTime()),
				company, currencyFormatter.format(amount));
	}
}
