import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileReadAndReplace {

    @Test
    public void Test_Compress_To_Zip_After_Text_Replcaement_In_Sample_FIle() throws IOException {

        String filePath = "src/main/resources/SampleFile.rtf";
        String directoryToFile = "../Kesava_SendGmail_ReplacedContent_Auditoria";
        FindAndReplace(filePath);
        zipDirectoryToSend(directoryToFile);
    }

    private static void FindAndReplace(String filePath) throws IOException {
        HashMap<String, String> map = new HashMap();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int dupkey = 0;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length >= 2) {
                String key = parts[0];
                String value = parts[1];
                map.put(key, value);
            }
        }
        Map<String, String> Tax_Identification = map.entrySet().stream().filter(k -> k.getKey().endsWith("Identification")).collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue()));
        Map<String, String> Types_Of_Tax_Identification = map.entrySet().stream().filter(k -> k.getKey().contains("Type of Tax Identification Number")).filter(j -> j.getValue().startsWith(" ")).collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue()));


        for (Map.Entry<String, String> find_tax_identification : Tax_Identification.entrySet()) {
            try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
                String h = find_tax_identification.getValue().intern();
                List<String> replaced = lines
                        .map(line2 -> line2.replaceAll(h, "222-33-4444"))
                        .collect(Collectors.toList());
                Files.write(Paths.get(filePath), replaced);
            }
        }

        for (Map.Entry<String, String> find_type_of_tax_identification : Types_Of_Tax_Identification.entrySet()) {
            try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
                String h = find_type_of_tax_identification.getValue();
                List<String> replaced = lines
                        .map(line2 -> line2.replaceAll(h, "ITIN_ITIN"))
                        .collect(Collectors.toList());

                Files.write(Paths.get(filePath), replaced);
            }
        }
        reader.close();
    }

    private static void zipDirectoryToSend(String filePath) throws IOException {

        FileOutputStream fos = new FileOutputStream("kesav_assignment_auditoria.zi_");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(filePath);

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }


}
