package com.dbscanner.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class RunnerTest {

	public static void main(String arg[]) {

		CommonSteps commonStps = new CommonSteps();
		ConfigFileReader config = new ConfigFileReader();

		DateFormat dateFormat = new SimpleDateFormat(config.getDateFormat());
		String date = dateFormat.format(new Date());
		long startTime = System.currentTimeMillis();

		String tableNameElement;
		String schema;
		String allTableOrNot = "";
		String attribute;
		String DBType;
		String ip;
		String port;
		String DBName;
		String userNamePassword;
		ArrayList<String> tableName = new ArrayList<String>();
		ArrayList<String> attributeList = new ArrayList<String>();
		Scanner scanner = new Scanner(System.in);
		long totalColoumnValue = 0;

		DbScanner ver = new DbScanner();
		
		commonStps.startPropertyFile();

		String countValue = "";
		System.out.println("Please enter DB type(Example:POSTGRE,ORACLE,MYSQL)");
		DBType = scanner.nextLine();
		if (DBType.equals("MYSQL") || DBType.equals("ORACLE")) {
			countValue = "count(*)";
		} else {
			countValue = "count";
		}
		
		System.out.println("Please enter DB IP");
		ip = scanner.nextLine();
		
		System.out.println("Please enter DB Port");
		port = scanner.nextLine();
		
		System.out.println("Please enter DB Name");
		DBName = scanner.nextLine();
		
		ver.updateIP(ip, port, DBName);

		System.out.println("Please enter schema name(Example:"+config.getSchemaName()+")");
		schema = scanner.nextLine();
		String[] schemaValue = schema.split(",");
		String[] constantSchemaValue = config.getSchemaName().split(",");
		for (String c : schemaValue) {
			boolean isFound = false;
			for (String b : constantSchemaValue) {
				if (c.toUpperCase().equals(b)) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				System.out.println("Please enter valid schema name");
				System.exit(0);
			}
		}
		
		System.out.println("Please enter username amd password (Example:username1,password1/username2,password2)");
		userNamePassword=scanner.nextLine();
		String[] userPassword = userNamePassword.split("/");

		for (int a = 0; a < schemaValue.length; a++) {

			if (a != 0) {
				commonStps.resetConstant();
				totalColoumnValue = 0;
			}
			
			String[] userPass= userPassword[a].split(",");
			ver.updateUsrPass(userPass[0], userPass[1]);
			ver.createDB(schemaValue[a],DBType);

			if (schemaValue.length <= 1) {
				System.out.println("Do you want to scan all tables in schema(Y/N)?");
				allTableOrNot = scanner.nextLine();
				if (!(allTableOrNot.toUpperCase().equals("Y") || allTableOrNot.toUpperCase().equals("N"))) {
					System.out.println("Please enter valid value");
					System.exit(0);
				}
			}

			if (allTableOrNot.toUpperCase().equals("Y") || schemaValue.length > 1) {
				tableName = commonStps.getTable(userPass[0],DBType);
				if(tableName.isEmpty()){
					System.out.println("Didn't captured table list from "+commonStps.saveQuery+" query");
				}
			} else {
				System.out.println("Please enter table names:");
				tableNameElement = scanner.nextLine();
				String[] sparatedValue = tableNameElement.split(",");
				for (int i = 0; i < sparatedValue.length; i++) {
					ArrayList<String> tmpTable = commonStps.getTable(userPass[0],DBType);
					String tmpValue = sparatedValue[i];
					if (tmpTable.contains(tmpValue)) {
						if (!config.getRemoveTable().contains(tmpValue)) {
							tableName.add(sparatedValue[i]);
						}
					} else {
						System.out.println("Please enter valid table names. " + sparatedValue[i] + " is invalid");
						System.exit(0);
					}
				}
			}

			ver.saveAllTableName(tableName);

			if (a == 0) {
				System.out.println("What attribute do you want to search (Plain card number,Track 2)?");
				attribute = scanner.nextLine();
				String[] attributeValue = attribute.split(",");
				for (int i = 0; i < attributeValue.length; i++) {
					String tmpValue = attributeValue[i].replaceAll("\\s", "").toUpperCase();
					if (tmpValue.equals("PLAINCARDNUMBER") || tmpValue.equals("TRACK2")) {
						attributeList.add(tmpValue);
					} else {
						System.out.println("Please enter valid attributes");
						System.exit(0);
					}
				}
			}

			ver.updateAttrtFunction(attributeList);

			for (int i = 0; i < tableName.size(); i++) {
				if (!config.getRemoveTable().contains(tableName.get(i).toLowerCase())) {
					String Query = "";
					if (DBType.equals("ORACLE")) {
						Query = "select COLUMN_NAME from ALL_TAB_COLUMNS WHERE table_name = '" + tableName.get(i)
								+ "'AND owner = '" + userPass[0] + "'";
					} else {
						Query = "SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
								+ tableName.get(i) + "'" + " AND table_schema='"
								+ schemaValue[a] + "';";
					}

					String queryLimit = "";
					ArrayList<String> newValue = ver.getDbValue(schemaValue[a], Query, "column_name",DBType,tableName.get(i));
					
					ArrayList<String> countarray = ver.getDbValue(schemaValue[a],
							"select count(*) from " + tableName.get(i), countValue,DBType,tableName.get(i));

					for (int k = 0; k < newValue.size(); k++) {
						try{
							int count = Integer.parseInt(countarray.get(0));
							ArrayList<Integer> countSeparateArray = ver.getCount(count);
							for (int j = 0; j < countSeparateArray.size(); j++) {
								ver.allColoumnScan=true;
								String constant = "";
								if (countSeparateArray.size() == 1 || j == 0) {
									constant = "a";
									queryLimit = ver.limitQuery(tableName.get(i), newValue.get(k),
											countSeparateArray.get(0), constant,DBType );
									ver.isValidationValue(schemaValue[a], queryLimit, newValue.get(k), tableName.get(i), 0,DBType);
								} else {
									constant = "b";
									queryLimit = ver.limitQuery(tableName.get(i), newValue.get(k),
											countSeparateArray.get(j - 1), constant,DBType);
									ver.isValidationValue(schemaValue[a], queryLimit, newValue.get(k), tableName.get(i),
											countSeparateArray.get(j - 1),DBType);
								}

							}
							if (countSeparateArray.size() > 0) {
								totalColoumnValue++;
							}
							if(ver.allColoumnScan){
								System.out.println("The scan was completed to " + newValue.get(k) + " coloumn in "
										+ tableName.get(i) + " table");
							}
							else
								System.out.println("The scan was failed to " + newValue.get(k) + " coloumn in "
										+ tableName.get(i) + " table");
						}catch(Exception e){
							System.out.println("The scan was failed to " + newValue.get(k) + " coloumn in "
									+ tableName.get(i) + " table");
						}
						
					}
				}

			}

			ver.updatefailedTableOnly();
			ver.closeDBConnection();

			if (attributeList.contains("PLAINCARDNUMBER")) {

				System.out.println("Starting to write in " + config.getReportDataFilePathPlanCard() + "_"
						+ commonStps.plCrdCurrPgCount + ".xls");
				commonStps.initiateXSSFWorkbook();
				ver.isCardNumber();
				ver.updateCardNumberExisttableName();

				ArrayList<ArrayList<String>> summary = new ArrayList<>();

				ArrayList<String> summaryData1 = new ArrayList<>();
				summaryData1.add("Service start up time");
				summaryData1.add(date);
				summaryData1.add("0");
				summaryData1.add("0");

				ArrayList<String> summaryData2 = new ArrayList<>();
				summaryData2.add("Service end time");
				summaryData2.add(dateFormat.format(new Date()));
				summaryData2.add("1");
				summaryData2.add("0");

				ArrayList<String> summaryData4 = new ArrayList<>();
				summaryData4.add("Scan table count");
				summaryData4.add(Integer.toString(tableName.size()));
				summaryData4.add("3");
				summaryData4.add("0");

				ArrayList<String> summaryData5 = new ArrayList<>();
				summaryData5.add("Scan total coloumn count");
				summaryData5.add(Long.toString(totalColoumnValue));
				summaryData5.add("4");
				summaryData5.add("0");

				ArrayList<String> summaryData6 = new ArrayList<>();
				summaryData6.add("Scan total cell count");
				summaryData6.add(Long.toString(ver.scanColCount));
				summaryData6.add("5");
				summaryData6.add("0");

				ArrayList<String> summaryData7 = new ArrayList<>();
				summaryData7.add("Found plain card number count");
				summaryData7.add(Long.toString(ver.lengthSixteenValue.size()));
				summaryData7.add("6");
				summaryData7.add("0");

				long finishTime = System.currentTimeMillis();
				long timePeriod = finishTime - startTime;
				int h = (int) ((timePeriod / 1000) / 3600);
				int m = (int) (((timePeriod / 1000) / 60) % 60);
				int s = (int) ((timePeriod / 1000) % 60);

				ArrayList<String> summaryData3 = new ArrayList<>();
				summaryData3.add("Scan time period from millisecond");
				summaryData3.add(h + " Hours " + m + " Minutes " + s + " Seconds (" + timePeriod + " Milliseconds )");
				summaryData3.add("2");
				summaryData3.add("0");

				summary.add(summaryData1);
				summary.add(summaryData2);
				summary.add(summaryData3);
				summary.add(summaryData4);
				summary.add(summaryData5);
				summary.add(summaryData6);
				summary.add(summaryData7);

				commonStps.initiateSheet("Summary");
				commonStps.updateExcel(summary, "PLAINCARDNUMBER", schemaValue[a]);

				commonStps.initiateSheet("Details");
				commonStps.updateDetailsSheettoplncrd(schemaValue[a]);
				commonStps.savePlainCardNumberToExcel(schemaValue[a]);

				commonStps.closeXSSFWorkbook();

			}

			if (attributeList.contains("TRACK2")) {

				System.out.println("Starting to write in " + config.getReportDataFilePathTrack2() + "_"
						+ commonStps.trc2CurrPgCount + ".xls");

				commonStps.initiateXSSFWorkbook();
				ver.updatetrack2ExisttableName();

				ArrayList<ArrayList<String>> summary = new ArrayList<>();

				ArrayList<String> summaryData1 = new ArrayList<>();
				summaryData1.add("Service start up timee");
				summaryData1.add(date);
				summaryData1.add("0");
				summaryData1.add("0");

				ArrayList<String> summaryData2 = new ArrayList<>();
				summaryData2.add("Service end time");
				summaryData2.add(dateFormat.format(new Date()));
				summaryData2.add("1");
				summaryData2.add("0");

				ArrayList<String> summaryData4 = new ArrayList<>();
				summaryData4.add("Scan table count");
				summaryData4.add(Integer.toString(tableName.size()));
				summaryData4.add("3");
				summaryData4.add("0");

				ArrayList<String> summaryData5 = new ArrayList<>();
				summaryData5.add("Scan total coloumn count");
				summaryData5.add(Long.toString(totalColoumnValue));
				summaryData5.add("4");
				summaryData5.add("0");

				ArrayList<String> summaryData6 = new ArrayList<>();
				summaryData6.add("Scan total cell count");
				summaryData6.add(Long.toString(ver.scanColCount));
				summaryData6.add("5");
				summaryData6.add("0");

				ArrayList<String> summaryData7 = new ArrayList<>();
				summaryData7.add("Found track2 count");
				summaryData7.add(Long.toString(ver.track2Count));
				summaryData7.add("6");
				summaryData7.add("0");

				long finishTime = System.currentTimeMillis();
				long timePeriod = finishTime - startTime;
				int h = (int) ((timePeriod / 1000) / 3600);
				int m = (int) (((timePeriod / 1000) / 60) % 60);
				int s = (int) ((timePeriod / 1000) % 60);

				ArrayList<String> summaryData3 = new ArrayList<>();
				summaryData3.add("Scan time period from millisecond");
				summaryData3.add(h + " Hours " + m + " Minutes " + s + " Seconds (" + timePeriod + " Milliseconds )");
				summaryData3.add("2");
				summaryData3.add("0");

				summary.add(summaryData1);
				summary.add(summaryData2);
				summary.add(summaryData3);
				summary.add(summaryData4);
				summary.add(summaryData5);
				summary.add(summaryData6);
				summary.add(summaryData7);

				commonStps.initiateSheet("Summary");
				commonStps.updateExcel(summary, "TRACK2", schemaValue[a]);

				commonStps.initiateSheet("Details");
				commonStps.updateDetailsSheettotrc2(schemaValue[a]);
				commonStps.updatTrack2eData(schemaValue[a]);

				commonStps.closeXSSFWorkbook();
			}
		}
		ver.getFileList();
		for (int i = 0; i < ver.fileArray.size(); i++) {
			commonStps.sendMail(attributeList, ver.fileArray.get(i));
		}
		if (ver.removeFile.size() > 0) {
			for (int i = 0; i < ver.removeFile.size(); i++) {
				System.out.println(ver.removeFile.get(i) + " report has file size more than 25MB");
			}
		}

		System.out.println("finished");
	}
}
