/*
 * Copyright 2014-2015 Yahoo! Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yahoo.validatar.report.junit;

import com.yahoo.validatar.parse.yaml.YAML;
import com.yahoo.validatar.parse.ParseManager;
import com.yahoo.validatar.common.TestSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.util.NodeComparator;

public class JUnitFormatterTest {
    @Test
    public void testWriteReport() throws FileNotFoundException, IOException, org.dom4j.DocumentException {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("DATE","20140807");

        ParseManager manager = new ParseManager();

        List<TestSuite> testSuites = ParseManager.expandParameters(manager.load((new File("src/test/resources/sample-tests/"))),
                                                                   paramMap);

        Assert.assertEquals(testSuites.size(), 3);

        // Ensure that we are modifying the 'Simple examples' test suite
        com.yahoo.validatar.common.Test test;
        if (testSuites.get(0).name.equals("Simple examples")) {
            test = testSuites.get(0).tests.get(1);
        } else {
            test = testSuites.get(1).tests.get(1);
        }

        test.setTestFailed();
        test.setFailedAssertMessage("Sample fail message");

        // Generate the test report file
        String[] args = {"--report-file", "target/JenkinsOutputTest.xml"};
        JUnitFormatter jUnitFormatter = new JUnitFormatter();
        jUnitFormatter.setup(args);
        jUnitFormatter.writeReport(testSuites);

        // Diff the test report file, and the expected output
        String hudsonOutput = new String(Files.readAllBytes(Paths.get("target/JenkinsOutputTest.xml")));
        Document hudsonOutputDom = DocumentHelper.parseText(hudsonOutput);

        String expectedHudsonOutput = new String(Files.readAllBytes(Paths.get("src/test/resources/ExpectedJenkinsOutput.xml")));
        Document expectedHudsonOutputDom = DocumentHelper.parseText(expectedHudsonOutput);

        NodeComparator comparator = new NodeComparator();
        if (comparator.compare(expectedHudsonOutputDom, hudsonOutputDom) != 0) {
            Assert.fail("The generated XML does not match expected XML!");
        }
    }
}