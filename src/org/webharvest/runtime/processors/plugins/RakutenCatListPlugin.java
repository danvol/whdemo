package org.webharvest.runtime.processors.plugins;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.webharvest.definition.XmlNode;
import org.webharvest.definition.XmlParser;
import org.webharvest.exception.FileException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.xml.sax.InputSource;

public class RakutenCatListPlugin extends WebHarvestPlugin {
	
	@Override
	public String getName() {
		return "rakutencat";
	}
	
    final String CAT_NO_KEY = "category_no";
    final String CAT_NAME_KEY = "category_name";
    final String PAR_CAT_NO_KEY = "parent_category_no";
    final String LEVEL_KEY = "level";
    
	@SuppressWarnings("unchecked")
	@Override
	public Variable executePlugin(Scraper scraper, ScraperContext context) {
        String dir = evaluateAttribute("dir", scraper);
        boolean isWriteHeader = evaluateAttributeAsBoolean("writeheader", true, scraper);
        String delimiter = evaluateAttribute("delimiter", scraper);
        String charset = evaluateAttribute("charset", scraper);
        String fileName = evaluateAttribute("filename", scraper);
        
        Variable body = executeBody(scraper, context);
        if(body==null){
        	return new NodeVariable( "<?xml version=\"1.0\"?><wrapper>NG</wrapper>");
        }

        try {
    		// ensure that target directory exists
    		new File( CommonUtil.getDirectoryFromPath(dir + "rakuten_body.txt") ).mkdirs();
    		
    		FileOutputStream out = new FileOutputStream(dir + "rakuten_body.txt", false);
    		byte[] data;
    		data = body.toString().getBytes(charset);
    		
    		out.write(data);
    		out.flush();
    		out.close();
    	} catch (IOException e) {
    		throw new FileException("Error writing data to file: " + dir + "rakuten_body.txt", e);
    	}
        
        List<String> headerNames = new ArrayList<String>();
        headerNames.add(CAT_NO_KEY);
        headerNames.add(CAT_NAME_KEY);
        headerNames.add(PAR_CAT_NO_KEY);
//        headerNames.add(LEVEL_KEY);
        final String newLine = System.getProperty("line.separator");
        List<Category> lines = new ArrayList<Category>();
        Iterator<Variable> it = body.toList().listIterator();
    	while (it.hasNext()) {
    		Variable currElement = it.next();
    		StringBuilder xmlBody = new StringBuilder();
    		xmlBody.append("<?xml version=\"1.0\"?><tempWrapper>");
    		xmlBody.append(currElement.toString());
    		xmlBody.append("</tempWrapper>");
        	XmlNode xmlNode = null;
			try {
				xmlNode = XmlParser.parse(new InputSource(new ByteArrayInputStream(xmlBody.toString().getBytes(charset))));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	    	Iterator<XmlNode> it2 = xmlNode.getElementList().iterator();
	    	while (it2.hasNext()) {
	    		XmlNode n = it2.next();
	    		Map<String, String> mapCommon = new HashMap<String, String>();
	    		for (Object obj : n.getElementList()) {
	    			XmlNode elm = (XmlNode)obj;
	    			if(!"list".equalsIgnoreCase(elm.getName())){
	    				mapCommon.put(elm.getName(), elm.getText());
	    			}
	    		}
	    		List<Map<String, String>> lst = new ArrayList<Map<String, String>>();
	    		for (Object obj : n.getElementList()) {
	    			XmlNode elm = (XmlNode)obj;
	    			if("list".equalsIgnoreCase(elm.getName())){
	    		    	Iterator<XmlNode> it3 = elm.getElementList().iterator();
	    		    	Map<String, String> map = new HashMap<String, String>();
	    		    	while (it3.hasNext()) {
	    		    		XmlNode n3 = it3.next();
	    		    		map.put(n3.getName(), n3.getText());
	    		    	}
	    		    	map.putAll(mapCommon);
	    		    	lst.add(map);
	    			}
	    		}
	    		if(lst.isEmpty()) lst.add(mapCommon);
	    		for (Map<String, String> m : lst) {
	    			while(m.size()>0){
	    				Category cat = getCategory(m);
	    				if (!lines.contains(cat)) lines.add(cat);
	    			}
	    		}
	    		
	    	}
    	}
    	
    	Collections.sort(lines, new DataComparator());
    	
    	List<String> outputLines = new ArrayList<String>();
    	for(Category c : lines){
    		StringBuilder s = new StringBuilder();
    		s.append(c.catNo).append(delimiter);
    		s.append(StringEscapeUtils.escapeCsv(c.catName.replaceAll("\\（.*\\）", ""))).append(delimiter);
    		s.append(c.parCatNo);
//    		s.append(c.parCatNo).append(delimiter);
//    		s.append(c.level);
    		outputLines.add(s.toString());
    	}
    	
        // CSV
        String fullPath = dir + fileName;
        CommonUtil.writeCsv(fileName, fullPath, charset, newLine
        		, StringUtils.join(headerNames.toArray(), delimiter), outputLines);
        return new NodeVariable( "<?xml version=\"1.0\"?><wrapper>OK</wrapper>");
	}
	
    Category getCategory(Map<String, String> map){
    	Category cat = new Category();
    	int max = 0;
		for (String key : map.keySet()) {
			int num = Integer.parseInt(key.replace(CAT_NO_KEY+"_", "").replace(CAT_NAME_KEY+"_", ""));
			if(max < num){
				max = num;
			}
		}
		String val = map.get(CAT_NO_KEY+"_"+max);
		if(!NumberUtils.isNumber(val)){
			val = "0";
		}
		cat.catNo = Long.valueOf(val);
		map.remove(CAT_NO_KEY+"_"+max);
		cat.catName = map.get(CAT_NAME_KEY+"_"+max);
		map.remove(CAT_NAME_KEY+"_"+max);
		if(max==0){
			cat.parCatNo=0L;
			cat.level=1;
		}else{
			val = map.get(CAT_NO_KEY+"_"+(max-1));
			if(!NumberUtils.isNumber(val)){
				val = "0";
			}
			cat.parCatNo=Long.valueOf(val);
			cat.level=max+1;
		}
    	return cat;
    }
    
    public class Category {
    	long catNo = 0L;
    	String catName = "";
    	long parCatNo = 0L;
    	int level = 0;
    	
    	@Override
    	public boolean equals(Object obj){
    		if(obj instanceof Category){
    			Category c = (Category) obj;
    			if (c.catNo==this.catNo 
    					&& c.parCatNo == this.parCatNo 
    					&& c.level == this.level 
    					&& c.catName.equals(this.catName)) {
    				return true;
    			}
    		}
    		return false;
    	}
    }
    
    public class DataComparator implements java.util.Comparator{
    	  public int compare(Object o1, Object o2){
    		  long cn1 = ((Category)o1).catNo;
    		  long cn2 = ((Category)o2).catNo;
    		  long pcn1 = ((Category)o1).parCatNo;
    		  long pcn2 = ((Category)o2).parCatNo;
    	    return Long.valueOf(cn1+pcn1).compareTo(cn2+pcn2);
    	  }
    	}
    
	@Override
    public String[] getValidAttributes() {
        return new String[] {
                "dir",
                "writeheader",
                "delimiter",
                "charset",
                "filename"};
    }

	@Override
    public String[] getRequiredAttributes() {
        return new String[] {
        		"dir", "writeheader", "delimiter", "charset", "filename"
        		};
    }
}
