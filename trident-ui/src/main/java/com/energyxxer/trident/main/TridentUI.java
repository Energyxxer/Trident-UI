package com.energyxxer.trident.main;

import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.global.Resources;
import com.energyxxer.trident.global.Status;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.trident.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.trident.ui.editor.completion.snippets.SnippetManager;
import com.energyxxer.util.ImageManager;
import com.energyxxer.util.logger.Debug;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;

public class TridentUI {
	public static final String LICENSE = "MIT License\n" +
			"\n" +
			"Copyright (c) 2019 Energyxxer\n" +
			"\n" +
			"Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
			"of this software and associated documentation files (the \"Software\"), to deal\n" +
			"in the Software without restriction, including without limitation the rights\n" +
			"to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
			"copies of the Software, and to permit persons to whom the Software is\n" +
			"furnished to do so, subject to the following conditions:\n" +
			"\n" +
			"The above copyright notice and this permission notice shall be included in all\n" +
			"copies or substantial portions of the Software.\n" +
			"\n" +
			"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
			"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
			"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
			"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
			"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
			"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
			"SOFTWARE.\n";

	public static TridentUI trident;
	public static final String UI_VERSION = "0.1.0";
	public static final String MIXED_VERSION = "u" + UI_VERSION + "c" + TridentCompiler.TRIDENT_LANGUAGE_VERSION;
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	public static TridentWindow window;

	private TridentUI() {
		window = new TridentWindow();
	}

	public static void main(String[] args) {
		Debug.addStream(System.out);

		try {
            Debug.addStream(new FileOutputStream(new File(Preferences.LOG_FILE_PATH)));
        } catch(FileNotFoundException x) {
		    Debug.log("Unable to open log file '" + Preferences.LOG_FILE_PATH + "', will not log to it until restarted");
        }
		Debug.log("Running on Java " + System.getProperty("java.version"));

		JFrame splash = new JFrame();
		splash.setSize(new Dimension(700, 410));
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		center.x -= 700/2;
		center.y -= 410/2;
		splash.setLocation(center);
		splash.setUndecorated(true);
		splash.setVisible(true);
		splash.setContentPane(new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				g.drawImage(ImageManager.load("/assets/logo/splash.png"), 0,0,this.getWidth(),this.getHeight(), null);

				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(new Color(187, 187, 187));
				g.setFont(g.getFont().deriveFont(21f));
				g.drawString(MIXED_VERSION, 512, 320);
				g.dispose();
			}
		});
		splash.revalidate();
		splash.setIconImage(ImageManager.load("/assets/logo/logo.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));

		Resources.load();

		trident = new TridentUI();
		System.setErr(new PrintStream(new OutputStream() {
			StringBuilder sb = new StringBuilder();

			@Override
			public void flush() throws IOException {
				String message = sb.toString();

				if(message.startsWith("Exception in thread ")) {
					Debug.log("", Debug.MessageType.PLAIN);
					TridentWindow.setStatus(new Status("error", message));
				}
				//defaultErrStream.println(sb.toString());
				Debug.log(message, Debug.MessageType.PLAIN);

				sb.setLength(0);
			}

			@Override
			public void write(int b) throws IOException {
				if(b == '\n') {
					flush();
				} else {
					sb.append((char) b);
				}
			}
		}));

		TridentWindow.setVisible(true);

        splash.setVisible(false);
        splash.dispose();

        if(Preferences.get("workspace_dir", null) == null) {
            WorkspaceDialog.prompt();
        }

        ProjectManager.loadWorkspace();

		TridentWindow.welcomePane.tipScreen.start(1000);
		TridentWindow.tabManager.openSavedTabs();
		TridentWindow.projectExplorer.openExplorerTree();
		SnippetManager.load();

		if(DefinitionUpdateProcess.CHECK_FOR_DEF_UPDATES_STARTUP.get()) DefinitionUpdateProcess.tryUpdate();

		ConsoleBoard.registerCommandHandler("license", new ConsoleBoard.CommandHandler() {
			@Override
			public String getDescription() {
				return "Displays the Trident UI license";
			}

			@Override
			public void printHelp() {
				Debug.log();
				Debug.log("LICENSE: Displays the Trident UI license");
			}

			@Override
			public void handle(String[] args) {
				Debug.log("\n"+LICENSE);
			}
		});
	}

}
