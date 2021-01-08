package com.zhilai.driver.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

public class IniReader {

    private LinkedHashMap<String, OrderedProperties> sections = new LinkedHashMap<>();

    private transient String currentSection;
    private transient OrderedProperties currentProperties;
    private String fileName;

    public IniReader(String fileName) throws IOException {
        init(fileName);
    }

    private void init(String fileName) throws IOException {

        sections.clear();

        this.fileName = fileName;

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;

        while ((line = br.readLine()) != null) {
            parseLine(line);
        }

        br.close();
    }

    private void parseLine(String line) {
        line = line.trim();

        if (line.startsWith("[") && line.endsWith("]")) {
            int start = line.indexOf("[");
            int end = line.indexOf("]");

            currentSection = line.substring(start + 1, end);
            currentProperties = new OrderedProperties();
            sections.put(currentSection, currentProperties);

        } else if (line.matches(".*=.*")) {
            if (currentProperties != null) {
                int i = line.indexOf('=');
                String name = line.substring(0, i);
                String value = line.substring(i + 1);
                currentProperties.setProperty(name, value);
            }
        }
    }

    public String[] getAllSections() {

        Set<String> keys = sections.keySet();
        String[] array = new String[keys.size()];

        Iterator<String> iterator = keys.iterator();

        int i = 0;
        while (iterator.hasNext()) {
            array[i++] = iterator.next();
        }

        return array;
    }

    public String getValue(String section, String key) {
        OrderedProperties p = sections.get(section);

        if (p == null) {
            return null;
        }

        return p.getProperty(key);
    }

    public boolean addSection(String section, String[] keys, String[] values) {

        if (keys.length != values.length || keys.length == 0) {
            return false;
        }

        OrderedProperties properties;

        if (sections.containsKey(section)) {
            properties = sections.get(section);
        } else {
            properties = new OrderedProperties();
        }

        for (int i = 0; i < keys.length; i++) {
            properties.setProperty(keys[i], values[i]);
        }
        sections.put(section, properties);
        return save();
    }

    public void deleteSection(String section) {
        sections.remove(section);
        save();
    }

    public boolean setValue(String section, String key, String value) {

        if (!sections.containsKey(section)) {
            return false;
        }

        OrderedProperties p = sections.get(section);

        p.setProperty(key, value);

        return save();

    }

    public String getContent() {

        StringBuilder sb = new StringBuilder();
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return sb.toString();
    }

    public String getContent(String section) {

        StringBuilder sb = new StringBuilder();
        OrderedProperties p = sections.get(section);

        if (p == null) {
            return null;
        }

        Set<Object> objects = p.keySet();
        for (Object o : objects) {
            sb.append(o + ":" + p.getProperty(String.valueOf(o)) + "\n");
        }

        return sb.toString();
    }

    public String getContentStart(String section) {
        StringBuilder sb = new StringBuilder();
        String[] allSections = getAllSections();
        for (String tem : allSections
                ) {
            if (tem.startsWith(section)) {
                sb.append("[" + tem + "]\n");
                sb.append(getContent(tem));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public boolean setValue(String section, String[] keys, String[] values) {

        OrderedProperties properties = sections.get(section);
        if (properties == null) {
            properties = new OrderedProperties();
            sections.put(section, properties);
        }

        int notSame = 0;

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = values[i];
            Object o = properties.setProperty(key, value);
            if (o == null || !o.equals(value)) {
                notSame++;
            }
        }
        if (notSame == 0) {
            return false;
        }
        return save();
    }

    /**
     * @param section
     * @param keys
     * @param values
     * @return true equal
     * false not equal
     */
    public boolean compareValue(String section, String[] keys, String[] values) {

        OrderedProperties properties = sections.get(section);
        if (properties == null) {
            return false;
        }
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = values[i];
            if (!properties.getProperty(key).equals(value)) {
                return false;
            }
        }
        return true;
    }

    private synchronized boolean save() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

            String[] allSections = getAllSections();

            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < allSections.length; i++) {
                sb.append("[" + allSections[i] + "]").append("\n");

                Properties properties = sections.get(allSections[i]);

                if (properties != null) {
                    Set<String> names = properties.stringPropertyNames();

                    for (String name : names) {
                        sb.append(name + "=" + properties.getProperty(name)).append("\n");
                    }
                }

                sb.append("\n");
            }

            bw.write(sb.toString());
            bw.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
