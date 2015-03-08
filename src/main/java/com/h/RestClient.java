package com.h;

import static com.jayway.restassured.RestAssured.get;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.jayway.restassured.response.Response;

public class RestClient {

	private static BigDecimal grandTotal = new BigDecimal(0);
	private static int RECORDS_PER_PAGE = 10;
	private static String HOST_URL = "http://henrychsiao.com/rest/%s.json";

	public static void main(String[] args) throws JsonParseException,
			JsonMappingException, IOException {

		int warnings = 0;
		Response resp = getReponse();
		if (resp == null) {
			warnings++;
		} else {
			int totalCount = resp.path("totalCount");
			BigDecimal totalBalance = new BigDecimal(resp.path("totalBalance")
					.toString());

			if (totalCount > 0) {
				warnings += process(totalCount);
			}

			printResults(totalBalance);
		}

		if (warnings > 0) {
			System.err.println("An error has occured. Please check logs.");
		}
	}

	private static Response getReponse() {
		Response resp = null;
		try {
			resp = get(String.format(HOST_URL, 1)).then().statusCode(200)
					.extract().response();
		} catch (AssertionError e) {
			System.out
					.println("Server offline. Unable to find transactions records.");
		}
		return resp;
	}

	private static void printResults(BigDecimal totalBalance) {
		if (totalBalance.compareTo(grandTotal) == 0) {
			System.out
					.println(String
							.format("Your Balance is accurate. You currently have a balance of $%.2f",
									grandTotal.doubleValue()));
		} else {
			System.out
					.println(String
							.format("There is a discrepency on your balance [$%.2f] and transactions [$%.2f].",
									totalBalance.doubleValue(),
									grandTotal.doubleValue()));
		}
	}

	private static int process(int totalCount) {
		int warnings = 0;
		System.out.println(String.format("Found total of %s records",
				totalCount));
		int pages = (totalCount / RECORDS_PER_PAGE) + 1;
		List<Transaction> list = new ArrayList<Transaction>();
		Set<String> ledgerType = new HashSet<String>();
		ArrayList<Object> transactions = null;
		int i = 1;
		for (; i <= pages; i++) {
			System.out.println(String.format("Processing Page %s of %s.", i,
					pages));
			try {
				transactions = get(String.format(HOST_URL, i)).then()
						.statusCode(200).extract().path("transactions");

				processTransactions(list, ledgerType, transactions);

			} catch (AssertionError e) {
				warnings++;
				System.err.println(String.format("Error accessing %s%s.json",
						HOST_URL, i));
			}
		}

		Collections.sort(list);

		System.out.println("");
		System.out.println("Total Items: " + list.size());
		System.out.println("Total Ledger Types: " + ledgerType.size());

		for (String type : ledgerType) {
			getTransactionsTypes(list, type);
		}

		return warnings;
	}

	private static void processTransactions(List<Transaction> list,
			Set<String> ledgerType, ArrayList<Object> transactions) {
		System.out.println(transactions);
		Iterator<Object> itr = transactions.iterator();
		while (itr.hasNext()) {
			HashMap record = (HashMap) itr.next();
			Transaction newTransaction = new Transaction();
			newTransaction.setDate(record.get("Date").toString());
			newTransaction.setAmount(new BigDecimal(record.get("Amount")
					.toString()));
			newTransaction.setCompany(record.get("Company").toString().trim());
			newTransaction.setLedger(record.get("Ledger").toString());
			ledgerType.add(record.get("Ledger").toString());
			list.add(newTransaction);
		}
	}

	private static void getTransactionsTypes(List<Transaction> list,
			String ledger) {

		Iterator<Transaction> listItr = list.iterator();
		List<Transaction> transactionList = new ArrayList<Transaction>();
		BigDecimal totalExpense = new BigDecimal(0);

		while (listItr.hasNext()) {
			Transaction transaction = listItr.next();
			if (transaction.getLedger().equalsIgnoreCase(ledger)) {
				transactionList.add(transaction);
				totalExpense = totalExpense.add(transaction.getAmount());
			}
		}
		grandTotal = grandTotal.add(totalExpense);

		printLedgerHeading(ledger, totalExpense);

		for (Transaction trans : transactionList) {
			System.out.println(trans.toString());
		}
		System.out.println();
	}

	private static void printLedgerHeading(String ledger,
			BigDecimal totalExpense) {
		if (ledger.isEmpty()) {
			ledger = "Payment";
		} else if (!ledger.contains("Expense")) {
			ledger += " Expense";
		}
		System.out.println(String.format("Total %s: %s", ledger,
				totalExpense.doubleValue()));
	}
}
