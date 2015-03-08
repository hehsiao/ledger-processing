package com.h;

import static com.jayway.restassured.RestAssured.get;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.jayway.restassured.response.Response;

public class RestClient {

	private static BigDecimal grandTotal = new BigDecimal(0);
	private static int INITIAL_PAGE = 1;
	private static int RECORDS_PER_PAGE = 10;
	private static String HOST_URL = "http://henrychsiao.com/rest/%s.json";

	public static void main(String[] args) throws JsonParseException,
			JsonMappingException, IOException {

		int warnings = 0;
		Response resp = getReponse(INITIAL_PAGE);
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

	private static Response getReponse(int page) {
		Response resp = null;
		String url = String.format(HOST_URL, page);
		try {
			resp = get(url).then().statusCode(200).extract().response();
		} catch (AssertionError e) {
			System.out.println("Error accessing " + url);
		}
		return resp;
	}

	private static int process(int totalCount) {
		int warnings = 0;
		int totalRecords = 0;
		System.out.println(String.format("Found total of %s records",
				totalCount));
		int pages = (totalCount / RECORDS_PER_PAGE) + 1;
		Map<String, List<Transaction>> ledgerType = new HashMap<String, List<Transaction>>();
		ArrayList<Object> transactions = null;

		int i = INITIAL_PAGE;
		for (; i <= pages; i++) {
			System.out.println(String.format("Processing Page %s of %s.", i,
					pages));
			try {
				transactions = getReponse(i).path("transactions");
				totalRecords = processTransactions(ledgerType, transactions);

			} catch (NullPointerException e) {
				warnings++;
				System.err.println("Unable to find transactions in response.");
			}
		}

		System.out.println("");
		System.out.println("Total Items: " + totalRecords);
		System.out.println("Total Ledger Types: " + ledgerType.size());

		for (Entry<String, List<Transaction>> list : ledgerType.entrySet()) {
			getTransactionsTypes(list.getValue(), list.getKey());
		}

		return warnings;
	}

	private static int processTransactions(
			Map<String, List<Transaction>> ledgerType,
			ArrayList<Object> transactions) {
		int count = 0;
		System.out.println(transactions);
		Iterator<Object> itr = transactions.iterator();
		while (itr.hasNext()) {
			count++;
			HashMap record = (HashMap) itr.next();
			Transaction newTransaction = new Transaction();
			newTransaction.setDate(record.get("Date").toString());
			newTransaction.setAmount(new BigDecimal(record.get("Amount")
					.toString()));
			newTransaction.setCompany(record.get("Company").toString().trim());
			String ledger = cleanLedger(record.get("Ledger").toString());

			newTransaction.setLedger(ledger);

			if (ledgerType.containsKey(ledger)) {
				ledgerType.get(ledger).add(newTransaction);
			} else {
				List<Transaction> newList = new ArrayList<Transaction>();
				newList.add(newTransaction);
				ledgerType.put(ledger, newList);
			}
		}
		return count;
	}

	private static String cleanLedger(String ledger) {
		if (ledger.isEmpty()) {
			ledger = "Payment";
		} else if (!ledger.contains("Expense")) {
			ledger += " Expense";
		}
		return ledger;
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
		System.out.println(String.format("Total %s: %s", ledger,
				totalExpense.doubleValue()));
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

}
