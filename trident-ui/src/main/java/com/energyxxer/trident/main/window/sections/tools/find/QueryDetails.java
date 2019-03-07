package com.energyxxer.trident.main.window.sections.tools.find;

import com.energyxxer.trident.main.TridentUI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryDetails {
    private String query;
    private boolean matchCase;
    private boolean wordsOnly;
    private boolean regex;
    private File rootFile;
    private int maxResults = -1;

    private FindResults results = null;

    private Predicate<File> fileNameFilter;

    public QueryDetails(String query, boolean matchCase, boolean wordsOnly, boolean regex, File rootFile) {
        this.query = query;
        this.matchCase = matchCase;
        this.wordsOnly = wordsOnly;
        this.regex = regex;
        this.rootFile = rootFile;
    }

    public FindResults perform() {
        search();
        return results;
    }

    private void search() {
        results = new FindResults();
        String rawPattern = query;
        if(rawPattern.isEmpty()) return;
        if(!regex) {
            rawPattern = Pattern.quote(rawPattern);
        }
        if(wordsOnly) {
            rawPattern = "\\b" + rawPattern + "\\b";
        }
        Pattern pattern = Pattern.compile(rawPattern, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
        searchInFile(pattern, rootFile);
    }

    private void searchInFile(Pattern query, File file) {
        if(maxResults > 0 && results.getCount() >= maxResults) return;
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files != null) {
                for(File child : files) {
                    searchInFile(query, child);
                    if(maxResults > 0 && results.getCount() >= maxResults) return;
                }
            }
        } else {
            if(fileNameFilter.test(file)) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()), TridentUI.DEFAULT_CHARSET);
                    Matcher matcher = query.matcher(content);
                    while(matcher.find()) {
                        int snippetStart = content.lastIndexOf('\n', matcher.start()-1);
                        if(snippetStart < 0) snippetStart = 0;
                        int snippetEnd = content.indexOf('\n', matcher.end());
                        if(snippetEnd > content.length()) snippetEnd = content.length();
                        int line = content.substring(0, snippetStart).split("\n",-1).length;
                        results.insertResult(new FileOccurrence(file, matcher.start(), matcher.end() - matcher.start(), line, content.substring(snippetStart, snippetEnd), matcher.start() - snippetStart));
                        if(maxResults > 0 && results.getCount() >= maxResults) return;
                    }
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }
        }
    }


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isWordsOnly() {
        return wordsOnly;
    }

    public void setWordsOnly(boolean wordsOnly) {
        this.wordsOnly = wordsOnly;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public File getRootFile() {
        return rootFile;
    }

    public void setRootFile(File rootFile) {
        this.rootFile = rootFile;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public FindResults getResults() {
        return results;
    }

    public void setResults(FindResults results) {
        this.results = results;
    }

    public Predicate<File> getFileNameFilter() {
        return fileNameFilter;
    }

    public void setFileNameFilter(Predicate<File> fileNameFilter) {
        this.fileNameFilter = fileNameFilter;
    }
}
