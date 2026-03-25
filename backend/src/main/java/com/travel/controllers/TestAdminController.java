package com.travel.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/admin/tests")
@PreAuthorize("hasRole('ADMIN')")
public class TestAdminController {

    private static final String TEST_PATH = "src/test/java";

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAvailableTests() {
        List<Map<String, Object>> testClasses = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(Paths.get(TEST_PATH))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith("Test.java"))
                 .forEach(path -> {
                     Map<String, Object> testClass = parseTestFile(path);
                     if (testClass != null) {
                        testClasses.add(testClass);
                     }
                 });
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        
        return ResponseEntity.ok(testClasses);
    }

    private Map<String, Object> parseTestFile(Path path) {
        try {
            String content = Files.readString(path);
            String fileName = path.getFileName().toString();
            String className = fileName.replace(".java", "");
            
            // Resolve full class name (package + class)
            String packageName = "";
            Matcher pkgMatcher = Pattern.compile("package\\s+([^;]+);").matcher(content);
            if (pkgMatcher.find()) {
                packageName = pkgMatcher.group(1).trim();
            }
            String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;

            Map<String, Object> classData = new HashMap<>();
            classData.put("name", className);
            classData.put("fullClassName", fullClassName);
            
            List<Map<String, String>> methods = new ArrayList<>();
            
            // Simple regex to find @Test methods and preceding comments
            // It looks for a comment block, then @Test, then the method signature
            Pattern methodPattern = Pattern.compile("(?:/\\*\\*?([^*]|[\\r\\n]|(?:\\*+([^*/]|[\\r\\n])))*\\*+/\\s+)?(?://.*\\s+)?@Test\\s+(?:public\\s+)?(?:void\\s+)?(\\w+)\\s*\\(");
            Matcher matcher = methodPattern.matcher(content);
            
            while (matcher.find()) {
                String comment = matcher.group(1);
                String methodName = matcher.group(3);
                
                String description = methodName;
                if (comment != null) {
                    // Normalize comment to a single line description
                    description = comment.replaceAll("\\*", "").replaceAll("\\r|\\n", " ").replaceAll("\\s+", " ").trim();
                    // If description is empty or just generic, use method name
                    if (description.isEmpty()) description = methodName;
                }

                Map<String, String> methodData = new HashMap<>();
                methodData.put("methodName", methodName);
                methodData.put("description", description);
                methods.add(methodData);
            }
            
            classData.put("methods", methods);
            return classData;
            
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runTest(@RequestBody Map<String, String> payload) {
        String testClass = payload.get("testClass");
        String testMethod = payload.get("testMethod");
        
        if (testClass == null || testClass.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Test class is required"));
        }

        String target = testClass;
        if (testMethod != null && !testMethod.isEmpty()) {
            target += "#" + testMethod;
        }

        StringBuilder output = new StringBuilder();
        boolean success = false;

        try {
            // Find project root (where pom.xml is)
            // If we are in 'touristpm', the pom is in 'backend/'
            // The directory structure is Downloads/7b Servidores/touristpm/backend
            // We'll execute from the directory that contains src/
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd", "/c", "mvn test -Dtest=" + target
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            success = (exitCode == 0);

        } catch (Exception e) {
            output.append("Error executing test: ").append(e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("output", output.toString());
        result.put("target", target);

        return ResponseEntity.ok(result);
    }
}
