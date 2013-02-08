package edu.atilim.acma;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class RunResultTest {
	
	public static void main(String[] args) throws IOException{
		RunResult rr = RunResult.readFrom("./data/results/HC-old/3cb2e034-de76-4051-aef4-cbba975df306.txt");
		System.out.println(rr.toCSVString());
		MpiWorkbook wb = new MpiWorkbook();
		HSSFSheet sheet = wb.createSheet("Results");
		HSSFRow row = null;
		int rowNo = 0;
		int columnNo = 0;
		NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);
		
		// Basliklar
		row = sheet.createRow(rowNo++);
		row.createCell(columnNo++).setCellValue("ID");
		row.createCell(columnNo++).setCellValue("Benchmark");
		row.createCell(columnNo++).setCellValue("Algorithm");
		row.createCell(columnNo++).setCellValue("Initial");
		row.createCell(columnNo++).setCellValue("Final");
		row.createCell(columnNo++).setCellValue("Time");
		row.createCell(columnNo++).setCellValue("Iterations");
		row.createCell(columnNo++).setCellValue("Restarts");
		row.createCell(columnNo++).setCellValue("Restart Depth");
		row.createCell(columnNo++).setCellValue("Population");
		row.createCell(columnNo++).setCellValue("Food Sources");
		row.createCell(columnNo++).setCellValue("Trials");
		row.createCell(columnNo++).setCellValue("Temperature");
		row.createCell(columnNo++).setCellValue("HC Algorithm Used");
		row.createCell(columnNo++).setCellValue("Swarm Size");
		row.createCell(columnNo++).setCellValue("AC1 - AC2");
		row.createCell(columnNo++).setCellValue("Absolute Gain");
		row.createCell(columnNo++).setCellValue("Relative Gain");
		
		row = sheet.createRow(rowNo++);
		columnNo = 0;
		
        InputStream is = RunResultTest.class.getResourceAsStream( "sample-json.txt");
        String jsonTxt = IOUtils.toString( is );
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON( jsonTxt );        
        double coolness = json.getDouble( "timeTaken" );
        String metricMode = json.getString( "metricMode" );    
        
        JSONObject design = json.getJSONObject("initialDesign");
        String score = design.getString("score");
        JSONObject summary = design.getJSONObject("metricSummary");
        String numOps = summary.getString("numOps");
        
        System.out.println( "metricMode: " + metricMode );
        System.out.println( "score: " + score );
        System.out.println( "numOps: " + numOps );
		
		
		
		row.createCell(columnNo++).setCellValue(rr.getAttribute("Benchmark", ""));
		row.createCell(columnNo++).setCellValue(rr.getAttribute("Algorithm", ""));
		row.createCell(columnNo++).setCellValue(nf.format(rr.getInitialDesign().getScore()));
		row.createCell(columnNo++).setCellValue(nf.format(rr.getFinalDesign().getScore()));
		row.createCell(columnNo++).setCellValue(rr.getAttribute("Time", ""));
		row.createCell(columnNo++).setCellValue(rr.getAttribute("Iterations", ""));
		row.createCell(columnNo++).setCellValue(rr.getAttribute("RestartCount", ""));
		row.createCell(columnNo++).setCellValue(rr.getAttribute("Randomization", ""));
		row.createCell(columnNo++).setCellValue(rr.getAttribute("Depth", ""));
		row.createCell(columnNo++).setCellValue(rr.getAttribute("MaxTrials", ""));
		
		//sb.append(id.toString()).append(';');
		FileOutputStream fos = new FileOutputStream(new File("ornek.xls"));

		wb.write(fos);
		
	}
	
	public static class MpiWorkbook extends HSSFWorkbook {
		public Map<String, HSSFCellStyle> styleMap = new HashMap<String, HSSFCellStyle>();
	}

}
