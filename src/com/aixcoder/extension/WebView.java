package com.aixcoder.extension;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserView;
import org.eclipse.ui.progress.UIJob;

import com.aixcoder.lib.Preference;

@SuppressWarnings("restriction")
public class WebView extends WebBrowserView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.aixcoder.extension.WebView";
	static WebView instance;

	public static WebView init() {
		if (instance == null || instance.viewer.isDisposed()) {
			try {
				instance = (WebView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView("com.aixcoder.extension.WebView");
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	@Inject
	IWorkbench workbench;

	boolean loaded = false;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new BrowserViewer(parent, 0);
		viewer.setContainer(this);

		initDragAndDrop();

		setBrowserViewName("aiXcoder Search");
		setBrowserViewTooltip("aiXcoder Search");
		setURL(Preference.getSearchEndpoint() + "?language=java&area=SpringBoot(Java)");
		viewer.getBrowser().addProgressListener(new ProgressListener() {

			@Override
			public void completed(ProgressEvent event) {
				loaded = true;
			}

			@Override
			public void changed(ProgressEvent event) {
				loaded = false;

			}
		});
	}

	public void doSearch(String txt) {
		String urlText = filterSearchInput(txt);
		String urlTextencoder;
		try {
			urlTextencoder = URLEncoder.encode(urlText, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			urlTextencoder = urlText;
		}
		final String q = urlTextencoder;
		if (!loaded) {
			try {
				viewer.getBrowser().evaluate("window.doSearch.toString()");
				loaded = true;
			} catch (Exception e) {
			}
		}
		if (loaded) {
			viewer.getBrowser().execute("window.doSearch('" + q + "','java','" + "SpringBoot(Java)" + "')");
		} else {
			new Job("aiXcoder Search Loading") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					long waitStart = Calendar.getInstance().getTimeInMillis();
					while (!loaded && Calendar.getInstance().getTimeInMillis() - waitStart < 10 * 1000) {
						try {
							System.out.println("!loaded");
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
					}
					new UIJob(Display.getDefault(), "aiXcoder Search") {

						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							try {
								System.out.println("execute2");
								try {
									String w = (String) viewer.getBrowser().evaluate("window.doSearch.toString()");
									System.out.println("execute2: " + w);
								} catch (Exception e) {
									e.printStackTrace();
								}
								viewer.getBrowser()
										.execute("window.doSearch('" + q + "','java','" + "SpringBoot(Java)" + "')");
								loaded = true;
							} catch (Exception e) {
								e.printStackTrace();
							}
							return Status.OK_STATUS;
						}
					}.schedule();
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	/**
	 * 过滤缩减被搜索原文：去掉对搜索相似代码无帮助的字符，去掉重复
	 *
	 * @param inputTxt
	 * @param language java, python, c
	 * @return
	 */
	private static String filterSearchInput(String inputTxt) {
		if (inputTxt == null) {
			return null;
		}
		StringBuilder returnSbd = new StringBuilder();
		String replaceRegex;// 替换对代码语义无用的字符，并拆分token

		List<String> blockList = new ArrayList<String>();
		List<String> lineList = new ArrayList<String>();

		try {
			inputTxt = cutComments(inputTxt, "//", lineList, "/*", "*/", blockList);
			replaceRegex = "[`~!@#$%^&*()+=|{}':;,\\[\\]<>/?！￥…（）—【】‘；：”“’。，、？]";// 函数调用的"点"不要切分，连起来包含语义，分开丢失很多

			int indexOfEnter = inputTxt.trim().indexOf("\n");
			if (indexOfEnter < 0) {
				return inputTxt.trim();// 如果只有一行，不要过滤 (不让用户看出来重排了)
			}

			Pattern p = Pattern.compile(replaceRegex);
			Matcher m = p.matcher(inputTxt);
			inputTxt = m.replaceAll(" ").trim();
			String[] tokenList = inputTxt.split(" ");
			Set<String> tokenSet = new HashSet<String>(Arrays.asList(tokenList));
			Object[] tokenArray = tokenSet.toArray();
			// 多行输入，过滤后保持中间至少有个换行
			int i = 0;
			for (; i < tokenArray.length; i++) {
				String currentToken = (String) tokenArray[i];
				if (currentToken == null) {
					continue;
				} else if (currentToken.trim().length() == 0) {
					continue;
				}
				currentToken = currentToken.trim();
				returnSbd.append(currentToken).append(" ").append("\n");// 多插入一个换行，为了搜索框中不显示过滤后的单行一堆单词
				i++;
				break;
			}
			for (; i < tokenArray.length; i++) {
				String currentToken = (String) tokenArray[i];
				if (currentToken == null) {
					continue;
				} else if (currentToken.trim().length() == 0) {
					continue;
				}
				currentToken = currentToken.trim();
				returnSbd.append(currentToken).append(" ");// 用换行而不是空格
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnSbd.toString();
	}

	/**
	 * @param inputStr             输入代码段
	 * @param lineCommentStr       行注释开头
	 * @param lineCommentList      截取的行注释List，如果不需要返回就填null
	 * @param blockCommentStartStr 块注释开头
	 * @param blockCommentEndStr   块注释结尾
	 * @param blockCommentList     截取的块注释List，如果不需要返回就填null
	 * @return 去掉Comments后的String
	 */
	private static String cutComments(String inputStr, String lineCommentStr, List<String> lineCommentList,
			String blockCommentStartStr, String blockCommentEndStr, List<String> blockCommentList) {
		if (inputStr == null) {
			return null;
		}
		if (inputStr.trim().length() == 0) {
			return "";
		}
		if (lineCommentList == null) {// 实际不返回
			lineCommentList = new ArrayList<String>();
		}
		if (blockCommentList == null) {// 实际不返回
			blockCommentList = new ArrayList<String>();
		}

		String outputStr = inputStr;
		int blockStartIndex;
		int blockEndIndex;
		String LINE_END_STR = "\n";
		int lineStartIndex;
		int lineEndIndex;

		try {
			if (blockCommentStartStr != null && blockCommentEndStr != null) {
				blockStartIndex = outputStr.indexOf(blockCommentStartStr);
				while (blockStartIndex >= 0) {
					blockEndIndex = outputStr.indexOf(blockCommentEndStr,
							blockStartIndex + blockCommentStartStr.length());
					if (blockEndIndex >= 0) {
						String beforeStr = outputStr.substring(0, blockStartIndex);
						String blockStr = outputStr.substring(blockStartIndex,
								blockEndIndex + blockCommentEndStr.length());
						String afterStr = outputStr.substring(blockEndIndex + blockCommentEndStr.length());
						blockCommentList.add(blockStr);
						outputStr = beforeStr + afterStr;
						blockStartIndex = outputStr.indexOf(blockCommentStartStr);
					} else {
						String beforeStr = outputStr.substring(0, blockStartIndex);
						String blockStr = outputStr.substring(blockStartIndex);
						blockCommentList.add(blockStr);
						outputStr = beforeStr;
						break;
					}
				}
			}
			// bug: 万一 注释行 中包含了 blockCommentStart，会有问题

			if (lineCommentStr != null) {
				lineStartIndex = outputStr.indexOf(lineCommentStr);
				while (lineStartIndex >= 0) {
					lineEndIndex = outputStr.indexOf(LINE_END_STR, lineStartIndex);
					if (lineEndIndex >= 0) {
						String beforeStr = outputStr.substring(0, lineStartIndex);
						String lineStr = outputStr.substring(lineStartIndex, lineEndIndex);
						String afterStr = outputStr.substring(lineEndIndex);
						lineCommentList.add(lineStr);
						outputStr = beforeStr + afterStr;
						lineStartIndex = outputStr.indexOf(lineCommentStr);
					} else {// 选中部分不一定到文件结尾或行结尾，所以可能没找到换行
						String beforeStr = outputStr.substring(0, lineStartIndex);
						String lineStr = outputStr.substring(lineStartIndex);
						lineCommentList.add(lineStr);
						outputStr = beforeStr;
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.printf("inputStr=%s\noutputStr=%s\nlineCommentSize=%d\nblockCommentSize=%d\n", inputStr,
					outputStr, lineCommentList.size(), blockCommentList.size());
			e.printStackTrace();
		}

		return outputStr;
	}
}
