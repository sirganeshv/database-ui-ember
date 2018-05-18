import java.util.HashMap;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRExportProgressMonitor;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInputItem;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.JRExporterParameter;

import java.util.HashMap;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.*;


public class Export{
	static int i = 0;
	static int pageNumber;
	static int totalPages = 0;
	static float progress;
	
	Export() {
		//System.out.println((float)pageNumber/(float)totalPages);
	}
	
	public void exportToPdf(String rawJsonData) {
	//public static void main(String args[]) {
		pageNumber = 0;
		totalPages = 0;
		@SuppressWarnings("rawtypes")
		JRAbstractExporter exporter = null;
		i = 0;
		System.out.println("Entering export module");
		HashMap hm = null;
		try {
			System.out.println("Start ....");
			String jrxmlFileName = "C:\\xampp\\tomcat\\webapps\\table_details\\design.jrxml";
			String jasperFileName = "sample.jasper";
			String pdfFileName = "D:\\eventDetails.pdf";

			JasperCompileManager.compileReportToFile(jrxmlFileName, jasperFileName);

			System.out.println("Report compiled");
			//String rawJsonDataa = "[{\"eventID\":\"4879\",\"eventProvider\":\"MSDTC Client 2\",\"eventType\":\"Warning\",\"timestamp\":\"02-05-2018 11:10:00\"},{\"eventID\":\"4879\",\"eventProvider\":\"MSDTC Client 2\",\"eventType\":\"Warning\",\"timestamp\":\"02-05-2018 11:12:48\"}]";
			//String rawJsonDataa = "[{\"eventID\":\"4879\",\"eventProvider\":\"MSDTC Client 2\"},{\"eventID\":\"4879\",\"eventProvider\":\"MSDTC Client 2\"}]";
			//,{"eventID":"2484","eventProvider":"Microsoft-Windows-Immersive-Shell","eventType":"Error","timestamp":"03-28-2018 19:32:37"},{"eventID":"2484","eventProvider":"Microsoft-Windows-Immersive-Shell","eventType":"Error","timestamp":"02-28-2018 21:39:39"},{"eventID":"2484","eventProvider":"Microsoft-Windows-Immersive-Shell","eventType":"Error","timestamp":"03-28-2018 19:32:35"}]"
			JasperReport report = (JasperReport) JRLoader.loadObject(new File("Sample.jasper"));
			ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(rawJsonData.getBytes());
			JsonDataSource ds = new JsonDataSource(jsonDataStream);
			Map parameters = new HashMap();
			parameters.put("title", "Jasper PDF Example");
			
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, ds);
			
			totalPages = jasperPrint.getPages().size();
			System.out.println("Report printed for "+jasperPrint.getPages().size()+" pages ");
			exporter = new JRPdfExporter();
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			createPageProgressMonitor(exporter);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, new FileOutputStream(new File(pdfFileName)));
			exporter.exportReport();

			System.out.println("Done exporting reports to pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setPageNumber(int no) {
		pageNumber = no;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}
	
	
	public float getProgress() {
		progress = (float)pageNumber/(float)totalPages;
		return progress;
	}
	
	private  void createPageProgressMonitor(JRAbstractExporter exporter)
	{
		exporter.setParameter(JRExporterParameter.PROGRESS_MONITOR, new JRExportProgressMonitor()
		{
			int pageCount = 0;
			@Override
			public  void afterPageExport()
			{
				pageCount++;
				setPageNumber(pageCount);
				System.out.println(pageCount);
				System.out.println(getProgress());
			}
		});
	}
}
	
	

//conf.setProgressMonitor(monitor)
			/*config = new SimplePdfReportConfiguration();
			i = config.getPageIndex();*/
			/*SimpleExporterInputItem exportInput = new SimpleExporterInputItem(jasperPrint);
			ReportExportConfiguration config = exportInput.getConfiguration();
			i = config.getPageIndex();*/
			
			

/*final JRExportProgressMonitor exportProgressMonitor = new JRExportProgressMonitor() {
				public void afterPageExport() {
					currentPageNum++;
					if (myListener != null) {
						myListener.afterPageExport(currentPageNum, pageCount);
						Thread.yield();
					}
				}
			};*/