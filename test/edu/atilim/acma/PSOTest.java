package edu.atilim.acma;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.DesignLoader;
import edu.atilim.acma.design.io.ZIPDesignReader;
import edu.atilim.acma.search.AbstractAlgorithm;
import edu.atilim.acma.search.HillClimbingForPSO;
import edu.atilim.acma.search.PSOAlgorithm;
import edu.atilim.acma.search.SolutionDesign;
import edu.atilim.acma.ui.ConfigManager;

public class PSOTest {

	public static class MpiWorkbook extends HSSFWorkbook {
		public Map<String, HSSFCellStyle> styleMap = new HashMap<String, HSSFCellStyle>();
	}

	public static void main(String[] args) throws IOException {
		String[] designNames = { "mosaic", "json" };
		int[] swarmSize = { 30, 50, 70, 90 };
		Double[] ac1 = { 2.05, 4.2, 8.2 };
		Double ac2 = 2.05;
		int[] w = { 1, 10, 20 };
		int[] iterationCount = { 20, 40, 60 };
		Double average;
		AbstractAlgorithm PSO = null;

		AbstractAlgorithm hcAlgorithm = new HillClimbingForPSO(null, null);
		DesignLoader loader = new ZIPDesignReader("./data/benchmarks/"+designNames[0]+".zip");
		Design design = loader.read();
		RunConfig runConfig = ConfigManager.runConfigs().get(0);
		int rowNo = 0;

		MpiWorkbook wb = new MpiWorkbook();
		HSSFSheet sheet = wb.createSheet("Results");
		HSSFRow row = null;
		
		// Basliklar
		row = sheet.createRow(rowNo++);
		row.createCell(0).setCellValue("Benchmark Files");
		row.createCell(1).setCellValue("HC Algorithms");
		row.createCell(2).setCellValue("Swarm Size");
		row.createCell(3).setCellValue("AC1");
		row.createCell(4).setCellValue("AC2");
		row.createCell(5).setCellValue("W");
		row.createCell(6).setCellValue("Iteration Count");
		row.createCell(7).setCellValue("Run Number");
		row.createCell(8).setCellValue("Score");
		
		for (int wNo = 0; wNo < w.length; wNo++)
			for (int ac1No = 0; ac1No < ac1.length; ac1No++) {
				average = 0.0;
				row = sheet.createRow(rowNo++);
				for (int runNo = 1; runNo <= 10; runNo++) {
					PSO = new PSOAlgorithm(new SolutionDesign(design, runConfig), null, hcAlgorithm, iterationCount[0], swarmSize[0], ac1[ac1No],
							ac2, w[wNo]);
					PSO.start(false);
					row = sheet.createRow(rowNo++);
					row.createCell(0).setCellValue(designNames[0]);
					row.createCell(1).setCellValue(hcAlgorithm.getName());
					row.createCell(2).setCellValue(swarmSize[0]);
					row.createCell(3).setCellValue(ac1[ac1No]);
					row.createCell(4).setCellValue(ac2);
					row.createCell(5).setCellValue(w[wNo]);
					row.createCell(6).setCellValue(iterationCount[0]);
					row.createCell(7).setCellValue(runNo);
					row.createCell(8).setCellValue(PSO.getFinalDesign().getScore());
					average += PSO.getFinalDesign().getScore();
				}
				row.createCell(9).setCellValue(average / 10.0);
			}

		// For Style
		for (int i = 0; i < 10; i++) {
			sheet.autoSizeColumn(i);
		}
		FileOutputStream fos = new FileOutputStream(new File(designNames[0]+".xls"));

		wb.write(fos);
		System.out.println("The End");
		return;
	}

}
