package com.dbscanner.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CommonSteps {

	ConfigFileReader configFileReader;
	DbScanner dbManager;
	
	Logger log = Logger.getLogger("LOG");

	static int summaryRowNumber = 0;

	public CommonSteps() {
		configFileReader = new ConfigFileReader();
		dbManager = new DbScanner();
	}

	static String reportDataFilePath = "";
	static int plCrdCurrPgCount = 1;
	static int trc2CurrPgCount = 1;
	static String saveQuery="";

	static XSSFWorkbook workbook = null;
	static XSSFSheet sheet = null;

	public void initiateXSSFWorkbook() {
		workbook = new XSSFWorkbook();
	}

	public void initiateSheet(String sheet1) {
		sheet = workbook.createSheet(sheet1);
	}

	public void closeXSSFWorkbook() {
		try {
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public void updateExcel(ArrayList<ArrayList<String>> bookData, String attribute, String schema) {

		if (attribute.equals("PLAINCARDNUMBER")) {
			reportDataFilePath = configFileReader.getReportDataFilePathPlanCard() + "_" + schema + "_"
					+ plCrdCurrPgCount + ".xls";
		} else if (attribute.equals("TRACK2")) {
			reportDataFilePath = configFileReader.getReportDataFilePathTrack2() + "_" + schema + "_" + trc2CurrPgCount
					+ ".xls";
		}
		try {
			ArrayList<Integer> numberArr = new ArrayList<Integer>();
			numberArr = getCount(bookData.size(), 100000);
			for (int j = 0; j < numberArr.size(); j++) {
				int rowCount = 0;
				int columnCount = 0;
				if (j == 0) {
					for (int k = 0; k < numberArr.get(j); k++) {
						rowCount = Integer.valueOf(bookData.get(k).get(bookData.get(k).size() - 2));
						Row row = sheet.createRow(rowCount);
						columnCount = Integer.valueOf(bookData.get(k).get(bookData.get(k).size() - 1));
						for (int i = 0; i < bookData.get(k).size() - 2; i++) {
							Cell cell = row.createCell(columnCount++);
							if (bookData.get(k).get(i) instanceof String) {
								cell.setCellValue(bookData.get(k).get(i));
							} else {
								cell.setCellValue(Integer.valueOf(bookData.get(k).get(i)));
							}
						}
					}
				} else {
					for (int k = numberArr.get(j - 1); k < numberArr.get(j); k++) {
						rowCount = Integer.valueOf(bookData.get(k).get(bookData.get(k).size() - 2));
						Row row = sheet.createRow(rowCount);
						columnCount = Integer.valueOf(bookData.get(k).get(bookData.get(k).size() - 1));
						for (int i = 0; i < bookData.get(k).size() - 2; i++) {
							Cell cell = row.createCell(columnCount++);
							if (bookData.get(k).get(i) instanceof String) {
								cell.setCellValue(bookData.get(k).get(i));
							} else {
								cell.setCellValue(Integer.valueOf(bookData.get(k).get(i)));
							}
						}
					}
				}

				try (FileOutputStream outputStream = new FileOutputStream(reportDataFilePath)) {
					autoSizeColumns();
					workbook.write(outputStream);
					outputStream.flush();
					outputStream.close();

				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}

			}

		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			e1.printStackTrace();
		}

	}

	public ArrayList<String> getTable(String schema, String DBType) {
		ArrayList<String> stableName = new ArrayList<String>();
		try {
			//String schemaName = getSchemaName(schema);
			String query = null;
			//String dbType = DBName;
			switch (DBType) {
			case "POSTGRE":
				query = "select tablename from pg_tables where schemaname = '" + schema + "'";
				break;
			case "ORACLE":
				query = "select table_name from user_tables";
				break;
			case "MYSQL":
				query = "SELECT table_name FROM information_schema.tables where table_schema='" + schema + "'";
			}
			if (DBType.equals("MYSQL"))
				stableName = dbManager.getTableValue(schema, query, "table_name",DBType);
			else if (DBType.equals("ORACLE")) {
				stableName = dbManager.getTableValue(schema, query, "table_name",DBType);
			} else {
				stableName = dbManager.getTableValue(schema, query, "tablename",DBType);
			}
			saveQuery=query;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return stableName;
	}

	public void savePlainCardNumberToExcel(String schema) {

		ArrayList<ArrayList<String>> headine = new ArrayList<>();
		ArrayList<String> headineData = new ArrayList<>();

		headineData.add("Table Name");
		headineData.add("Coloumn Name");
		headineData.add("Card Number");
		headineData.add("Coloumn Index Number");
		headineData.add("0");
		headineData.add("0");

		headine.add(headineData);

		try {
			if (!dbManager.masterCard.isEmpty() || !dbManager.visaCard.isEmpty() || !dbManager.amexCard.isEmpty()
					|| !dbManager.jcbCard.isEmpty() || !dbManager.upiCard.isEmpty() || !dbManager.maestroCard.isEmpty()
					|| !dbManager.otherCard.isEmpty()) {

				if (dbManager.plcrdpageCount() <= configFileReader.getpageCountConfg()) {
					if (!dbManager.masterCard.isEmpty()) {

						initiateSheet("MasterCardData");

						updateExcel(headine, "PLAINCARDNUMBER", schema);

						for (int i = 0; i < dbManager.masterCard.size(); i++) {
							dbManager.masterCard.get(i).add(Integer.toString(i + 2));
							dbManager.masterCard.get(i).add("0");

						}
						updateExcel(dbManager.masterCard, "PLAINCARDNUMBER", schema);
					}

					if (!dbManager.visaCard.isEmpty()) {

						initiateSheet("visaCardData");

						updateExcel(headine, "PLAINCARDNUMBER", schema);

						for (int i = 0; i < dbManager.visaCard.size(); i++) {
							dbManager.visaCard.get(i).add(Integer.toString(i + 2));
							dbManager.visaCard.get(i).add("0");

						}
						updateExcel(dbManager.visaCard, "PLAINCARDNUMBER", schema);
					}

					if (!dbManager.amexCard.isEmpty()) {

						initiateSheet("amexCardData");

						updateExcel(headine, "PLAINCARDNUMBER", schema);

						for (int i = 0; i < dbManager.amexCard.size(); i++) {
							dbManager.amexCard.get(i).add(Integer.toString(i + 2));
							dbManager.amexCard.get(i).add("0");

						}
						updateExcel(dbManager.amexCard, "PLAINCARDNUMBER", schema);
					}

					if (!dbManager.jcbCard.isEmpty()) {

						initiateSheet("jcbCardData");

						updateExcel(headine, "PLAINCARDNUMBER", schema);

						for (int i = 0; i < dbManager.jcbCard.size(); i++) {

							dbManager.jcbCard.get(i).add(Integer.toString(i + 2));
							dbManager.jcbCard.get(i).add("0");

						}
						updateExcel(dbManager.jcbCard, "PLAINCARDNUMBER", schema);
					}

					if (!dbManager.upiCard.isEmpty()) {

						initiateSheet("upiCardData");

						updateExcel(headine, "PLAINCARDNUMBER", schema);

						for (int i = 0; i < dbManager.upiCard.size(); i++) {
							dbManager.upiCard.get(i).add(Integer.toString(i + 2));
							dbManager.upiCard.get(i).add("0");

						}
						updateExcel(dbManager.upiCard, "PLAINCARDNUMBER", schema);
					}

					if (!dbManager.maestroCard.isEmpty()) {

						initiateSheet("maestroCardData");

						updateExcel(headine, "PLAINCARDNUMBER", schema);

						for (int i = 0; i < dbManager.maestroCard.size(); i++) {
							dbManager.maestroCard.get(i).add(Integer.toString(i + 2));
							dbManager.maestroCard.get(i).add("0");

						}
						updateExcel(dbManager.maestroCard, "PLAINCARDNUMBER", schema);

					}

					if (!dbManager.otherCard.isEmpty()) {

						initiateSheet("otherCard");

						updateExcel(headine, "PLAINCARDNUMBER", schema);

						for (int i = 0; i < dbManager.otherCard.size(); i++) {
							dbManager.otherCard.get(i).add(Integer.toString(i + 2));
							dbManager.otherCard.get(i).add("0");

						}
						updateExcel(dbManager.otherCard, "PLAINCARDNUMBER", schema);
					}
					System.out.println("finished to write in " + configFileReader.getReportDataFilePathPlanCard() + "_"
							+ plCrdCurrPgCount + ".xls");
					plCrdCurrPgCount++;
				} else {

					int pageCount = dbManager.plcrdpageCount() / configFileReader.getpageCountConfg();

					if (dbManager.plcrdpageCount() % configFileReader.getpageCountConfg() > 0) {
						pageCount++;
					}

					ArrayList<Integer> tmpmasterCardlength = new ArrayList<Integer>();
					tmpmasterCardlength = getCount((dbManager.masterCard.size()), configFileReader.getpageCountConfg());

					ArrayList<Integer> tmpvisaCardlength = new ArrayList<Integer>();
					tmpvisaCardlength = getCount((dbManager.visaCard.size()), configFileReader.getpageCountConfg());

					ArrayList<Integer> tmpamexCardlength = new ArrayList<Integer>();
					tmpamexCardlength = getCount((dbManager.amexCard.size()), configFileReader.getpageCountConfg());

					ArrayList<Integer> tmpjcbCardlength = new ArrayList<Integer>();
					tmpjcbCardlength = getCount((dbManager.jcbCard.size()), configFileReader.getpageCountConfg());

					ArrayList<Integer> tmpupiCardlength = new ArrayList<Integer>();
					tmpupiCardlength = getCount((dbManager.upiCard.size()), configFileReader.getpageCountConfg());

					ArrayList<Integer> tmpmaestroCardlength = new ArrayList<Integer>();
					tmpmaestroCardlength = getCount((dbManager.maestroCard.size()),
							configFileReader.getpageCountConfg());

					ArrayList<Integer> tmpotherCardlength = new ArrayList<Integer>();
					tmpotherCardlength = getCount((dbManager.otherCard.size()), configFileReader.getpageCountConfg());

					while (plCrdCurrPgCount <= pageCount) {

						if (plCrdCurrPgCount != 1) {
							System.out
									.println("Starting to write in " + configFileReader.getReportDataFilePathPlanCard()
											+ "_" + plCrdCurrPgCount + ".xls");
						}

						if (!dbManager.masterCard.isEmpty() && ((plCrdCurrPgCount - 1)
								* configFileReader.getpageCountConfg() < dbManager.masterCard.size())) {

							initiateSheet("MasterCardData");
							updateExcel(headine, "PLAINCARDNUMBER", schema);
							ArrayList<ArrayList<String>> tmpmasterCard = new ArrayList<ArrayList<String>>();

							if (plCrdCurrPgCount == 1) {
								for (int j = 0; j < tmpmasterCardlength.get(plCrdCurrPgCount - 1); j++) {
									ArrayList<String> tmpmasterCarddetails = new ArrayList<String>();
									tmpmasterCarddetails.add(dbManager.masterCard.get(j).get(0));
									tmpmasterCarddetails.add(dbManager.masterCard.get(j).get(1));
									tmpmasterCarddetails.add(dbManager.masterCard.get(j).get(2));
									tmpmasterCarddetails.add(dbManager.masterCard.get(j).get(3));
									tmpmasterCarddetails.add(Integer.toString(j + 2));
									tmpmasterCarddetails.add("0");
									tmpmasterCard.add(tmpmasterCarddetails);
								}
								updateExcel(tmpmasterCard, "PLAINCARDNUMBER", schema);
							}

							else {
								int columnnum = 0;
								for (int j = tmpmasterCardlength.get(plCrdCurrPgCount - 2); j < (tmpmasterCardlength
										.get(plCrdCurrPgCount - 1)); j++) {
									ArrayList<String> tmpmasterCarddetails = new ArrayList<String>();
									tmpmasterCarddetails.add(dbManager.masterCard.get(j).get(0));
									tmpmasterCarddetails.add(dbManager.masterCard.get(j).get(1));
									tmpmasterCarddetails.add(dbManager.masterCard.get(j).get(2));
									tmpmasterCarddetails.add(dbManager.masterCard.get(j).get(3));
									tmpmasterCarddetails.add(Integer.toString(columnnum++ + 2));
									tmpmasterCarddetails.add("0");
									tmpmasterCard.add(tmpmasterCarddetails);
								}
								updateExcel(tmpmasterCard, "PLAINCARDNUMBER", schema);

							}
							tmpmasterCard.clear();
						}

						if (!dbManager.visaCard.isEmpty() && ((plCrdCurrPgCount - 1)
								* configFileReader.getpageCountConfg() < dbManager.visaCard.size())) {

							initiateSheet("VisaCardData");
							updateExcel(headine, "PLAINCARDNUMBER", schema);
							ArrayList<ArrayList<String>> tmpvisaCard = new ArrayList<ArrayList<String>>();

							if (plCrdCurrPgCount == 1) {
								for (int j = 0; j < tmpvisaCardlength.get(plCrdCurrPgCount - 1); j++) {
									ArrayList<String> tmpvisaCarddetails = new ArrayList<String>();
									tmpvisaCarddetails.add(dbManager.visaCard.get(j).get(0));
									tmpvisaCarddetails.add(dbManager.visaCard.get(j).get(1));
									tmpvisaCarddetails.add(dbManager.visaCard.get(j).get(2));
									tmpvisaCarddetails.add(dbManager.visaCard.get(j).get(3));
									tmpvisaCarddetails.add(Integer.toString(j + 2));
									tmpvisaCarddetails.add("0");
									tmpvisaCard.add(tmpvisaCarddetails);
								}
								updateExcel(tmpvisaCard, "PLAINCARDNUMBER", schema);

							}

							else {
								int columnnum = 0;
								for (int j = tmpvisaCardlength.get(plCrdCurrPgCount - 2); j < (tmpvisaCardlength
										.get(plCrdCurrPgCount - 1)); j++) {
									ArrayList<String> tmpvisaCarddetails = new ArrayList<String>();
									tmpvisaCarddetails.add(dbManager.visaCard.get(j).get(0));
									tmpvisaCarddetails.add(dbManager.visaCard.get(j).get(1));
									tmpvisaCarddetails.add(dbManager.visaCard.get(j).get(2));
									tmpvisaCarddetails.add(dbManager.visaCard.get(j).get(3));
									tmpvisaCarddetails.add(Integer.toString(columnnum++ + 2));
									tmpvisaCarddetails.add("0");
									tmpvisaCard.add(tmpvisaCarddetails);
								}
								updateExcel(tmpvisaCard, "PLAINCARDNUMBER", schema);

							}
							tmpvisaCard.clear();
						}

						if (!dbManager.amexCard.isEmpty() && ((plCrdCurrPgCount - 1)
								* configFileReader.getpageCountConfg() < dbManager.amexCard.size())) {

							initiateSheet("AmexCardData");
							updateExcel(headine, "PLAINCARDNUMBER", schema);
							ArrayList<ArrayList<String>> tmpamexCard = new ArrayList<ArrayList<String>>();

							if (plCrdCurrPgCount == 1) {

								for (int j = 0; j < tmpamexCardlength.get(plCrdCurrPgCount - 1); j++) {
									ArrayList<String> tmpamexCarddetails = new ArrayList<String>();
									tmpamexCarddetails.add(dbManager.amexCard.get(j).get(0));
									tmpamexCarddetails.add(dbManager.amexCard.get(j).get(1));
									tmpamexCarddetails.add(dbManager.amexCard.get(j).get(2));
									tmpamexCarddetails.add(dbManager.amexCard.get(j).get(3));
									tmpamexCarddetails.add(Integer.toString(j + 2));
									tmpamexCarddetails.add("0");
									tmpamexCard.add(tmpamexCarddetails);
								}
								updateExcel(tmpamexCard, "PLAINCARDNUMBER", schema);
							}

							else {
								int columnnum = 0;
								for (int j = tmpamexCardlength.get(plCrdCurrPgCount - 2); j < (tmpamexCardlength
										.get(plCrdCurrPgCount - 1)); j++) {
									ArrayList<String> tmpamexCarddetails = new ArrayList<String>();
									tmpamexCarddetails.add(dbManager.amexCard.get(j).get(0));
									tmpamexCarddetails.add(dbManager.amexCard.get(j).get(1));
									tmpamexCarddetails.add(dbManager.amexCard.get(j).get(2));
									tmpamexCarddetails.add(dbManager.amexCard.get(j).get(3));
									tmpamexCarddetails.add(Integer.toString(columnnum++ + 2));
									tmpamexCarddetails.add("0");
									tmpamexCard.add(tmpamexCarddetails);
								}
								updateExcel(tmpamexCard, "PLAINCARDNUMBER", schema);
							}
							tmpamexCard.clear();
						}

						if (!dbManager.jcbCard.isEmpty() && ((plCrdCurrPgCount - 1)
								* configFileReader.getpageCountConfg() < dbManager.jcbCard.size())) {

							initiateSheet("JcbCardData");
							updateExcel(headine, "PLAINCARDNUMBER", schema);
							ArrayList<ArrayList<String>> tmpjcbCard = new ArrayList<ArrayList<String>>();

							if (plCrdCurrPgCount == 1) {

								for (int j = 0; j < tmpjcbCardlength.get(plCrdCurrPgCount - 1); j++) {
									ArrayList<String> tmpjcbCarddetails = new ArrayList<String>();
									tmpjcbCarddetails.add(dbManager.jcbCard.get(j).get(0));
									tmpjcbCarddetails.add(dbManager.jcbCard.get(j).get(1));
									tmpjcbCarddetails.add(dbManager.jcbCard.get(j).get(2));
									tmpjcbCarddetails.add(dbManager.jcbCard.get(j).get(3));
									tmpjcbCarddetails.add(Integer.toString(j + 2));
									tmpjcbCarddetails.add("0");
									tmpjcbCard.add(tmpjcbCarddetails);
								}
								updateExcel(tmpjcbCard, "PLAINCARDNUMBER", schema);
							}

							else {
								int columnnum = 0;
								for (int j = tmpjcbCardlength.get(plCrdCurrPgCount - 2); j < (tmpjcbCardlength
										.get(plCrdCurrPgCount - 1)); j++) {
									ArrayList<String> tmpjcbCarddetails = new ArrayList<String>();
									tmpjcbCarddetails.add(dbManager.jcbCard.get(j).get(0));
									tmpjcbCarddetails.add(dbManager.jcbCard.get(j).get(1));
									tmpjcbCarddetails.add(dbManager.jcbCard.get(j).get(2));
									tmpjcbCarddetails.add(dbManager.jcbCard.get(j).get(3));
									tmpjcbCarddetails.add(Integer.toString(columnnum++ + 2));
									tmpjcbCarddetails.add("0");
									tmpjcbCard.add(tmpjcbCarddetails);
								}
								updateExcel(tmpjcbCard, "PLAINCARDNUMBER", schema);
							}
							tmpjcbCard.clear();
						}

						if (!dbManager.upiCard.isEmpty() && ((plCrdCurrPgCount - 1)
								* configFileReader.getpageCountConfg() < dbManager.upiCard.size())) {

							initiateSheet("UpiCardData");
							updateExcel(headine, "PLAINCARDNUMBER", schema);
							ArrayList<ArrayList<String>> tmpupiCard = new ArrayList<ArrayList<String>>();

							if (plCrdCurrPgCount == 1) {

								for (int j = 0; j < tmpupiCardlength.get(plCrdCurrPgCount - 1); j++) {
									ArrayList<String> tmpupiCarddetails = new ArrayList<String>();
									tmpupiCarddetails.add(dbManager.upiCard.get(j).get(0));
									tmpupiCarddetails.add(dbManager.upiCard.get(j).get(1));
									tmpupiCarddetails.add(dbManager.upiCard.get(j).get(2));
									tmpupiCarddetails.add(dbManager.upiCard.get(j).get(3));
									tmpupiCarddetails.add(Integer.toString(j + 2));
									tmpupiCarddetails.add("0");
									tmpupiCard.add(tmpupiCarddetails);
								}
								updateExcel(tmpupiCard, "PLAINCARDNUMBER", schema);
							}

							else {
								int columnnum = 0;
								for (int j = tmpupiCardlength.get(plCrdCurrPgCount - 2); j < (tmpupiCardlength
										.get(plCrdCurrPgCount - 1)); j++) {
									ArrayList<String> tmpupiCarddetails = new ArrayList<String>();
									tmpupiCarddetails.add(dbManager.upiCard.get(j).get(0));
									tmpupiCarddetails.add(dbManager.upiCard.get(j).get(1));
									tmpupiCarddetails.add(dbManager.upiCard.get(j).get(2));
									tmpupiCarddetails.add(dbManager.upiCard.get(j).get(3));
									tmpupiCarddetails.add(Integer.toString(columnnum++ + 2));
									tmpupiCarddetails.add("0");
									tmpupiCard.add(tmpupiCarddetails);
								}
								updateExcel(tmpupiCard, "PLAINCARDNUMBER", schema);

							}
							tmpupiCard.clear();
						}

						if (!dbManager.maestroCard.isEmpty() && ((plCrdCurrPgCount - 1)
								* configFileReader.getpageCountConfg() < dbManager.maestroCard.size())) {

							initiateSheet("MaestroCardData");
							updateExcel(headine, "PLAINCARDNUMBER", schema);
							ArrayList<ArrayList<String>> tmpmaestroCard = new ArrayList<ArrayList<String>>();

							if (plCrdCurrPgCount == 1) {

								for (int j = 0; j < tmpmaestroCardlength.get(plCrdCurrPgCount - 1); j++) {
									ArrayList<String> tmpmaestroCarddetails = new ArrayList<String>();
									tmpmaestroCarddetails.add(dbManager.maestroCard.get(j).get(0));
									tmpmaestroCarddetails.add(dbManager.maestroCard.get(j).get(1));
									tmpmaestroCarddetails.add(dbManager.maestroCard.get(j).get(2));
									tmpmaestroCarddetails.add(dbManager.maestroCard.get(j).get(3));
									tmpmaestroCarddetails.add(Integer.toString(j + 2));
									tmpmaestroCarddetails.add("0");
									tmpmaestroCard.add(tmpmaestroCarddetails);
								}
								updateExcel(tmpmaestroCard, "PLAINCARDNUMBER", schema);
							}

							else {
								int columnnum = 0;
								for (int j = tmpmaestroCardlength.get(plCrdCurrPgCount - 2); j < (tmpmaestroCardlength
										.get(plCrdCurrPgCount - 1)); j++) {
									ArrayList<String> tmpmaestroCarddetails = new ArrayList<String>();
									tmpmaestroCarddetails.add(dbManager.maestroCard.get(j).get(0));
									tmpmaestroCarddetails.add(dbManager.maestroCard.get(j).get(1));
									tmpmaestroCarddetails.add(dbManager.maestroCard.get(j).get(2));
									tmpmaestroCarddetails.add(dbManager.maestroCard.get(j).get(3));
									tmpmaestroCarddetails.add(Integer.toString(columnnum++ + 2));
									tmpmaestroCarddetails.add("0");
									tmpmaestroCard.add(tmpmaestroCarddetails);
								}
								updateExcel(tmpmaestroCard, "PLAINCARDNUMBER", schema);

							}
							tmpmaestroCard.clear();
						}

						if (!dbManager.otherCard.isEmpty() && ((plCrdCurrPgCount - 1)
								* configFileReader.getpageCountConfg() < dbManager.otherCard.size())) {

							initiateSheet("OtherCardData");
							updateExcel(headine, "PLAINCARDNUMBER", schema);
							ArrayList<ArrayList<String>> tmpotherCard = new ArrayList<ArrayList<String>>();

							if (plCrdCurrPgCount == 1) {

								for (int j = 0; j < tmpotherCardlength.get(plCrdCurrPgCount - 1); j++) {
									ArrayList<String> tmpotherCarddetails = new ArrayList<String>();
									tmpotherCarddetails.add(dbManager.otherCard.get(j).get(0));
									tmpotherCarddetails.add(dbManager.otherCard.get(j).get(1));
									tmpotherCarddetails.add(dbManager.otherCard.get(j).get(2));
									tmpotherCarddetails.add(dbManager.otherCard.get(j).get(3));
									tmpotherCarddetails.add(Integer.toString(j + 2));
									tmpotherCarddetails.add("0");
									tmpotherCard.add(tmpotherCarddetails);
								}
								updateExcel(tmpotherCard, "PLAINCARDNUMBER", schema);

							}

							else {
								int columnnum = 0;
								for (int j = tmpotherCardlength.get(plCrdCurrPgCount - 2); j < (tmpotherCardlength
										.get(plCrdCurrPgCount - 1)); j++) {
									ArrayList<String> tmpotherCarddetails = new ArrayList<String>();
									tmpotherCarddetails.add(dbManager.otherCard.get(j).get(0));
									tmpotherCarddetails.add(dbManager.otherCard.get(j).get(1));
									tmpotherCarddetails.add(dbManager.otherCard.get(j).get(2));
									tmpotherCarddetails.add(dbManager.otherCard.get(j).get(3));
									tmpotherCarddetails.add(Integer.toString(columnnum++ + 2));
									tmpotherCarddetails.add("0");
									tmpotherCard.add(tmpotherCarddetails);
								}
								updateExcel(tmpotherCard, "PLAINCARDNUMBER", schema);

							}
							tmpotherCard.clear();
						}

						System.out.println("Finished to write in " + configFileReader.getReportDataFilePathPlanCard()
								+ "_" + plCrdCurrPgCount + ".xls");
						plCrdCurrPgCount++;

						if (plCrdCurrPgCount <= pageCount) {
							closeXSSFWorkbook();
							initiateXSSFWorkbook();
						}
					}
				}

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			dbManager.masterCard.clear();
			dbManager.visaCard.clear();
			dbManager.amexCard.clear();
			dbManager.jcbCard.clear();
			dbManager.upiCard.clear();
			dbManager.maestroCard.clear();
			dbManager.otherCard.clear();
		}
	}

	public void sendMail(ArrayList<String> attribute, ArrayList<String> report) {
		final String fromEmailUserName = configFileReader.getLoginEmailUserName();
		final String password = configFileReader.getLoginEmailPassword();

		Properties props = new Properties();
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmailUserName, password);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromEmailUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(configFileReader.getToEmailUserName()));
			message.setSubject("DB Scaner Report");

			Multipart multipart = new MimeMultipart();

			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setText("Hi,\n\nPlease find the attached DB Scaner Report");

			for (int i = 0; i < report.size(); i++) {
				MimeBodyPart attachmentBodyPart1 = new MimeBodyPart();
				DataSource source1 = new FileDataSource(report.get(i));
				attachmentBodyPart1.setDataHandler(new DataHandler(source1));
				attachmentBodyPart1.setFileName(report.get(i));
				multipart.addBodyPart(attachmentBodyPart1);
			}
			multipart.addBodyPart(textBodyPart);
			message.setContent(multipart);
			Transport.send(message);

		} catch (MessagingException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			e1.printStackTrace();
		}
	}

	public void updateDetailsSheettoplncrd(String schema) {

		ArrayList<ArrayList<String>> b = new ArrayList<>();

		ArrayList<String> a1 = new ArrayList<>();
		a1.add("Plain card number exist tables");
		a1.add("Plain card number exist coloumns");
		a1.add("0");
		a1.add("0");

		b.add(a1);

		updateExcel(b, "PLAINCARDNUMBER", schema);

		int existRowValue = 1;
		int notExistRowValue = 1;
		String tmpTblNm = "";

		for (int i = 0; i < dbManager.cardNumberExisttableName.size(); i++) {
			dbManager.cardNumberExisttableName.get(i).add(Integer.toString(i + 1));
			dbManager.cardNumberExisttableName.get(i).add("0");
		}

		updateExcel(dbManager.cardNumberExisttableName, "PLAINCARDNUMBER", schema);

		existRowValue = dbManager.cardNumberExisttableName.size() + 1;

		b = new ArrayList<>();
		a1 = new ArrayList<>();

		a1.add("Plain card number not exist tables");
		existRowValue++;
		a1.add(Integer.toString(existRowValue++));
		a1.add("0");

		b.add(a1);

		updateExcel(b, "PLAINCARDNUMBER", schema);

		dbManager.updatecardNumberNotExisttableName();

		b = new ArrayList<>();

		for (int i = 0; i < dbManager.cardNumberNotExisttableName.size(); i++) {

			a1 = new ArrayList<>();

			a1.add(dbManager.cardNumberNotExisttableName.get(i));
			a1.add(Integer.toString(existRowValue++));
			a1.add("0");

			b.add(a1);
		}

		updateExcel(b, "PLAINCARDNUMBER", schema);
		
		a1 = new ArrayList<>();
		a1.add("Scaned failed tables");
		a1.add("Scaned failed coloumns");
		existRowValue++;
		a1.add(Integer.toString(existRowValue++));
		a1.add("0");
		
		b = new ArrayList<>();
		b.add(a1);
		updateExcel(b, "PLAINCARDNUMBER", schema);
		
		
		b = new ArrayList<>();
		
		for(int k=0;k<dbManager.failedTable.size();k++){
			a1 = new ArrayList<>();
			a1.add(dbManager.failedTable.get(k).get(0));
			a1.add(dbManager.failedTable.get(k).get(1));
			a1.add(Integer.toString(existRowValue++));
			a1.add("0");
			b.add(a1);
		}
		
		updateExcel(b, "PLAINCARDNUMBER", schema);
		
	}

	public void updateDetailsSheettotrc2(String schema) {

		ArrayList<ArrayList<String>> b = new ArrayList<>();
		ArrayList<String> a1 = new ArrayList<>();

		a1.add("Track2 exist tables");
		a1.add("Track2 exist coloumns");
		a1.add("0");
		a1.add("0");

		b.add(a1);

		updateExcel(b, "TRACK2", schema);

		int existRowValue = 1;

		for (int i = 0; i < dbManager.track2ExisttableName.size(); i++) {
			dbManager.track2ExisttableName.get(i).add(Integer.toString(i + 1));
			dbManager.track2ExisttableName.get(i).add("0");
		}

		updateExcel(dbManager.track2ExisttableName, "TRACK2", schema);

		existRowValue = dbManager.track2ExisttableName.size() + 1;

		b = new ArrayList<>();
		a1 = new ArrayList<>();

		a1.add("Track2 not exist tables");
		existRowValue++;
		a1.add(Integer.toString(existRowValue++));
		a1.add("0");

		b.add(a1);

		updateExcel(b, "TRACK2", schema);

		dbManager.updateTrc2NotExisttableName();

		b = new ArrayList<>();

		for (int i = 0; i < dbManager.track2NotExisttableName.size(); i++) {
			a1 = new ArrayList<>();

			a1.add(dbManager.track2NotExisttableName.get(i));
			a1.add(Integer.toString(existRowValue++));
			a1.add("0");

			b.add(a1);
		}
		updateExcel(b, "TRACK2", schema);
		
		a1 = new ArrayList<>();
		a1.add("Scaned failed tables");
		a1.add("Scaned failed coloumns");
		existRowValue++;
		a1.add(Integer.toString(existRowValue++));
		a1.add("0");
		
		b = new ArrayList<>();
		b.add(a1);
		updateExcel(b, "TRACK2", schema);
		
		b = new ArrayList<>();
		
		for(int k=0;k<dbManager.failedTable.size();k++){
			a1 = new ArrayList<>();
			a1.add(dbManager.failedTable.get(k).get(0));
			a1.add(dbManager.failedTable.get(k).get(1));
			a1.add(Integer.toString(existRowValue++));
			a1.add("0");
			b.add(a1);
		}
		
		updateExcel(b, "TRACK2", schema);
	}

	public void updatTrack2eData(String schema) {
		if (!dbManager.track2Value.isEmpty()) {
			try {
				ArrayList<ArrayList<String>> headine = new ArrayList<>();
				ArrayList<String> headineData = new ArrayList<>();

				headineData.add("Track2 exist line number");
				headineData.add("Track2");
				headineData.add("File path");
				headineData.add("0");
				headineData.add("0");

				headine.add(headineData);

				if (dbManager.track2Value.size() <= configFileReader.getpageCountConfg()) {
					initiateSheet("Data");

					updateExcel(headine, "TRACK2", schema);

					for (int i = 0; i < dbManager.track2Value.size(); i++) {
						dbManager.track2Value.get(i).add(Integer.toString(i + 1));
						dbManager.track2Value.get(i).add("0");
					}
					updateExcel(dbManager.track2Value, "TRACK2", schema);
					System.out.println("Finished to write in " + configFileReader.getReportDataFilePathTrack2() + "_"
							+ trc2CurrPgCount + ".xls");
					trc2CurrPgCount++;
					closeXSSFWorkbook();

				} else {

					int pageCount = dbManager.track2Value.size() / configFileReader.getpageCountConfg();

					if (dbManager.track2Value.size() / configFileReader.getpageCountConfg() > 0) {
						pageCount++;
					}

					ArrayList<Integer> tmptrc2length = new ArrayList<Integer>();
					tmptrc2length = getCount((dbManager.track2Value.size() - 1), configFileReader.getpageCountConfg());

					while (trc2CurrPgCount <= pageCount) {

						if (trc2CurrPgCount != 1) {
							System.out.println("Starting to write in " + configFileReader.getReportDataFilePathTrack2()
									+ "_" + trc2CurrPgCount + ".xls");
						}
						initiateSheet("Data");
						updateExcel(headine, "TRACK2", schema);
						ArrayList<ArrayList<String>> tmptrc2 = new ArrayList<ArrayList<String>>();

						if (trc2CurrPgCount == 1) {

							for (int j = 0; j <= tmptrc2length.get(trc2CurrPgCount - 1); j++) {
								ArrayList<String> tmptrc2details = new ArrayList<String>();
								tmptrc2details.add(dbManager.track2Value.get(j).get(0));
								tmptrc2details.add(dbManager.track2Value.get(j).get(1));
								tmptrc2details.add(dbManager.track2Value.get(j).get(2));
								tmptrc2details.add(Integer.toString(j + 2));
								tmptrc2details.add("0");
								tmptrc2.add(tmptrc2details);
							}
							updateExcel(tmptrc2, "TRACK2", schema);
						}

						else {
							int columnnum = 0;
							for (int j = (tmptrc2length.get(trc2CurrPgCount - 2) + 1); j <= (tmptrc2length
									.get(trc2CurrPgCount - 1)); j++) {
								ArrayList<String> tmptrc2details = new ArrayList<String>();
								tmptrc2details.add(dbManager.track2Value.get(j).get(0));
								tmptrc2details.add(dbManager.track2Value.get(j).get(1));
								tmptrc2details.add(dbManager.track2Value.get(j).get(2));
								tmptrc2details.add(Integer.toString(columnnum++ + 2));
								tmptrc2details.add("0");
								tmptrc2.add(tmptrc2details);
							}
							updateExcel(tmptrc2, "TRACK2", schema);
						}
						System.out.println("finished to write in " + configFileReader.getReportDataFilePathTrack2()
								+ "_" + trc2CurrPgCount + ".xls");
						trc2CurrPgCount++;
						tmptrc2.clear();
						closeXSSFWorkbook();
						if (trc2CurrPgCount <= pageCount) {
							initiateXSSFWorkbook();
						}
					}
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			} finally {
				dbManager.track2Value.clear();
			}

		} else {
			System.out.println("finished to write in " + configFileReader.getReportDataFilePathTrack2() + "_"
					+ trc2CurrPgCount + ".xls");
			trc2CurrPgCount++;
		}
	}

	public void autoSizeColumns() {
		try {
			if (sheet.getPhysicalNumberOfRows() > 0) {
				Row row = sheet.getRow(0);
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					int columnIndex = cell.getColumnIndex();
					sheet.autoSizeColumn(columnIndex);
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}

	public ArrayList<Integer> getCount(int count, int constant) {
		ArrayList<Integer> countArray = new ArrayList<Integer>();
		int limitCount = constant;
		int a = 0;
		int b = 0;
		a = count / limitCount;

		for (int i = 1; i <= a; i++) {
			countArray.add(i * limitCount);
		}
		countArray.add(count);
		return countArray;
	}

	public void resetConstant() {
		plCrdCurrPgCount = 1;
		trc2CurrPgCount = 1;
		summaryRowNumber = 0;
		dbManager.saveValue = new ArrayList<String>();
		dbManager.lengthSixteenValue = new ArrayList<ArrayList<String>>();
		dbManager.track2Value = new ArrayList<ArrayList<String>>();
		dbManager.cardNumberExisttableName = new ArrayList<ArrayList<String>>();
		dbManager.cardNumberNotExisttableName = new ArrayList<String>();
		dbManager.allTableName = new ArrayList<String>();
		dbManager.track2ExisttableName = new ArrayList<ArrayList<String>>();
		dbManager.track2NotExisttableName = new ArrayList<String>();
		dbManager.masterCard = new ArrayList<ArrayList<String>>();
		dbManager.visaCard = new ArrayList<ArrayList<String>>();
		dbManager.amexCard = new ArrayList<ArrayList<String>>();
		dbManager.jcbCard = new ArrayList<ArrayList<String>>();
		dbManager.upiCard = new ArrayList<ArrayList<String>>();
		dbManager.maestroCard = new ArrayList<ArrayList<String>>();
		dbManager.otherCard = new ArrayList<ArrayList<String>>();
		dbManager.updateAttributeList = new ArrayList<String>();
		dbManager.fileArray = new ArrayList<ArrayList<String>>();
		dbManager.removeFile = new ArrayList<String>();
		dbManager.track2Count = 0;
		dbManager.scanColCount = 0;
		dbManager.failedTable=new ArrayList<ArrayList<String>>();
	}
	
	 public void startPropertyFile() {
		   PropertyConfigurator.configure("config/Configuration.properties");
	    }

}
