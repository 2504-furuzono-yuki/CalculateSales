package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_NAME_ERROR = "売上ファイル名が連番になっていません";
	private static final String FILE_SALES_ERROR = "合計⾦額が10桁を超えました";
	private static final String FILE_NUMBER_ERROR = "の支店コードが不正です";
	private static final String FILE_COMNUM_ERROR = "の商品コードが不正です";
	private static final String FILE_FORMAT_ERROR = "のフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//コマンドライン引数が渡されているか確認
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		// 商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, "^[0-9]{3}")) {
			return;
		}
		// 商品別集計ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_COMMODITY_LST, commodityNames, commoditySales, "^[A-Za-z0-9]+${8}")) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			//正規表現…数字8桁「[0-9]{8}」　　.rcd「.rcd$」
			if (files[i].isFile() && files[i].getName().matches("^[0-9]{8}[.]rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
		Collections.sort(rcdFiles);
		for (int i = 0; i < rcdFiles.size() - 1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			if ((latter - former) != 1) {
				System.out.println(FILE_NAME_ERROR);
				return;
			}
		}

		BufferedReader br = null;
		String line;
		for (int i = 0; i < rcdFiles.size(); i++) {
			try {

				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				List<String> list = new ArrayList<>();
				while ((line = br.readLine()) != null) {
					list.add(line);
				}

				if (list.size() != 3) {
					System.out.println(file.getName() + FILE_FORMAT_ERROR);
					return;
				}

				if (!branchNames.containsKey(list.get(0))) {
					System.out.println(file.getName() + FILE_NUMBER_ERROR);
					return;
				}

				if (!commodityNames.containsKey(list.get(1))) {
					System.out.println(file.getName() + FILE_COMNUM_ERROR);
					return;
				}

				if (!list.get(2).matches("^[0-9]*$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				long fileSale = Long.parseLong(list.get(2));
				//一旦足し算した値を、変数に入れる。　※最後はマップに入れたい
				Long saleAmount = branchSales.get(list.get(0)) + fileSale;
				Long amount = commoditySales.get(list.get(1)) + fileSale;

				if (saleAmount >= 10000000000L || amount >= 10000000000L) {
					System.out.println(FILE_SALES_ERROR);
					return;
				}

				//足し算した結果(変数saleAmount)を、マップsaleAmountに入れてあげる
				branchSales.put(list.get(0), saleAmount);
				commoditySales.put(list.get(1), amount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;//main	メソッドの処理を終わらせる

			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return; //main	メソッドの処理を終わらせる
					}
				}
			}
			// 支店別集計ファイル書き込み処理
			if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
				return;
			}
			// 商品別集計ファイル書き込み処理
			if (!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
				return;
			}
		}
	}//mainメソッド

	/**
	 * 支店定義ファイル読み込み処理
	 * @param string
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> namesMap,
			Map<String, Long> salesMap, String regularexpression) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			if (!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");
				if ((items.length != 2) || (!items[0].matches(regularexpression))) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

				namesMap.put(items[0], items[1]);
				salesMap.put(items[0], 0L);

			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;

		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> namesMap, Map<String, Long> salesMap) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter br = null;

		try {
			File file = new File(path, fileName);
			FileWriter fr = new FileWriter(file);
			br = new BufferedWriter(fr);

			for (String key : namesMap.keySet()) {
				br.write(key + "," + namesMap.get(key) + "," + salesMap.get(key));
				br.newLine();
			}
			return true;

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
	}
}
