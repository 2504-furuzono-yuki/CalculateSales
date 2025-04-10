package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {
			//正規表現…数字8桁「[0-9]{8}」　　.rcd「.rcd$」
			if(files[i].getName().matches("^[0-9]{8}.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
		BufferedReader br = null;

		try {
			String line;
			for(int i = 0; i < rcdFiles.size(); i++) {

				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				List<String> list = new ArrayList<>();

				while((line = br.readLine()) != null) {
					list.add(line);
				}

				long fileSale = Long.parseLong(list.get(1));
				//一旦足し算した値を、変数に入れる。　※最後はマップに入れたい
				Long saleAmount = branchSales.get(list.get(0)) + fileSale;

				//足し算した結果(変数saleAmount)を、マップsaleAmountに入れてあげる
				branchSales.put(list.get(0), saleAmount);
			}
		} catch(IOException e) {
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

			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。

			//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
			//※詳細は後述で説明
//			long fileSale = Long.parseLong(売上⾦額);

			//読み込んだ売上⾦額を加算します。
			//※詳細は後述で説明
//			Long saleAmount = branchSales.get(items[0]) + long に変換した売上⾦額;

			//加算した売上⾦額をMapに追加します。

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}  //mainメソッド


	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
			    String[] items = line.split(",");
			    branchNames.put(items[0], items[1]);
			    branchSales.put(items[0], 0L);
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
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter br = null;

		try {
			File file = new File(path, fileName);
			FileWriter fr = new FileWriter(file);
			br = new BufferedWriter(fr);

			for (String key : branchNames.keySet()) {
				br.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
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
