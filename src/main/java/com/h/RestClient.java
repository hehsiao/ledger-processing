package com.h;

import static com.jayway.restassured.RestAssured.get;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.jayway.restassured.response.Response;

/**
 * RestClient retrieves and categorize data obtained from JSON responses. The
 * output is displayed in console. The first response will contains the number
 * of total entries which then paginate by incrementing the number in the url.
 * 
 * @author Henry Hsiao
 * @since Dec 2014
 */
public class RestClient {

	private static BigDecimal grandTotal = new BigDecimal(0);
	private static final int INITIAL_PAGE = 1;
	private static final int RECORDS_PER_PAGE = 10;
	private static final String HOST_URL = "http://henrychsiao.com/rest/%s.json";
	private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
	private static final String TABLE_FORMAT = "%-15s%-50s%-10s";

	/**
	 * Request for JSON Response using RestAssured
	 * 
	 * @param page
	 * @return Response in JSON format
	 */
	private static Response getReponse(int page) {
		Response resp = null;
		String url = String.format(HOST_URL, page);
		try {
			resp = get(url).then().statusCode(200).extract().response();
		} catch (AssertionError e) {
			System.err.println("Error accessing " + url);
		}
		return resp;
	}

	/**
	 * Call and process each page of JSON response
	 * 
	 * @param pages
	 * @return warning count
	 */
	private static int processPage(int pages, int totalCount) {
		int warnings = 0;
		int totalRecords = 0;

		Map<String, List<Transaction>> ledgerType = new HashMap<String, List<Transaction>>();
		ArrayList<Object> transactions = null;

		int i = INITIAL_PAGE;
		for (; i <= pages; i++) {
			try {
				transactions = getReponse(i).path("transactions");
				totalRecords = processTransactions(ledgerType, transactions, totalRecords);

			} catch (NullPointerException e) {
				warnings++;
				System.err.println("Unable to find transactions in response.");
			}
		}

		for (Entry<String, List<Transaction>> list : ledgerType.entrySet()) {
			Collections.sort(list.getValue());
			printledgerDetails(list.getValue(), list.getKey());
		}

		if (totalRecords != totalCount) {
			System.err.println(String.format("Incorrect record counts. Expects: %s, Actual: %s", pages,
					totalRecords));
		}

		return warnings;
	}

	/**
	 * Process and store each transaction from JSON response into a Map
	 * 
	 * @param ledgerCategorizedMap
	 *          - <ledger Type, list of corresponding transactions>
	 * @param transactions
	 *          - all transactions from JSON response
	 * @param recordCount
	 * @return recordCount
	 */
	private static int processTransactions(Map<String, List<Transaction>> ledgerCategorizedMap,
			ArrayList<Object> transactions, int recordCount) {
		Iterator<Object> itr = transactions.iterator();
		while (itr.hasNext()) {
			recordCount++;

			HashMap record = (HashMap) itr.next();
			Transaction newTransaction = new Transaction();
			newTransaction.setDate(record.get("Date").toString());
			newTransaction.setAmount(new BigDecimal(record.get("Amount").toString()));
			newTransaction.setCompany(record.get("Company").toString().trim());
			String ledger = cleanLedger(record.get("Ledger").toString());
			newTransaction.setLedger(ledger);

			if (ledgerCategorizedMap.containsKey(ledger)) {
				ledgerCategorizedMap.get(ledger).add(newTransaction);
			} else {
				List<Transaction> newList = new ArrayList<Transaction>();
				newList.add(newTransaction);
				ledgerCategorizedMap.put(ledger, newList);
			}
		}
		return recordCount;
	}

	/**
	 * Certain ledger are blank or missing the word "Expense" to ensure the
	 * categorization is standardized
	 * 
	 * @param ledger
	 * @return cleaned ledger name
	 */
	private static String cleanLedger(String ledger) {
		if (ledger.isEmpty()) {
			ledger = "Payments";
		} else if (!ledger.contains("Expense")) {
			ledger += " Expense";
		}
		return ledger;
	}

	/**
	 * Output the organized lists
	 */
	private static void printledgerDetails(List<Transaction> list, String ledger) {

		BigDecimal totalExpense = new BigDecimal(0);

		System.out.println("===== " + ledger + " =====");
		System.out.println(String.format(TABLE_FORMAT, "Date", "Location", "Amount"));
		for (Transaction trans : list) {
			totalExpense = totalExpense.add(trans.getAmount());
			System.out.println(trans.printTable(TABLE_FORMAT));
		}
		System.out.println("Total: " + currencyFormatter.format(totalExpense) + "\n");

		grandTotal = grandTotal.add(totalExpense);
	}

	/**
	 * Output whether the stated balance was accurate after reconciling
	 * transactions.
	 * 
	 * @param totalBalance
	 */
	private static void printResults(BigDecimal totalBalance) {
		if (totalBalance.compareTo(grandTotal) == 0) {
			System.out.println(String.format(
					"Your Balance is accurate. You currently have a balance of %s.",
					currencyFormatter.format(grandTotal)));
		} else {
			System.out.println(String.format(
					"There is a discrepency on your balance [%s] and transactions [%s].",
					currencyFormatter.format(totalBalance), currencyFormatter.format(grandTotal)));
		}
	}

	public static void main(String[] args) throws JsonParseException, JsonMappingException,
			IOException {
		int warnings = 0;

		// Obtain the first response and ensure the source is working.
		Response resp = getReponse(INITIAL_PAGE);
		if (resp == null) {
			warnings++;

		} else {
			int totalCount = resp.path("totalCount");
			BigDecimal totalBalance = new BigDecimal(resp.path("totalBalance").toString());

			if (totalCount > 0) {
				int pages = (totalCount / RECORDS_PER_PAGE) + 1;
				warnings += processPage(pages, totalCount);
				printResults(totalBalance);
			} else {
				System.out.println("No transactions found.");
			}

		}

		if (warnings > 0) {
			System.err.println("An error has occured. Please check logs.");
		}
	}

}
