package com.slt.peotv.userservice.lms.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slt.peotv.userservice.lms.entity.RoleEntity;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import com.slt.peotv.userservice.lms.repository.RoleRepository;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.service.UserService;
import com.slt.peotv.userservice.lms.shared.Messaging.UserEventPublisher;
import com.slt.peotv.userservice.lms.shared.Roles;
import com.slt.peotv.userservice.lms.shared.Utils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Utils utils;
    @Autowired
    private UserService userService;
    @Autowired
    private UserEventPublisher userEventPublisher;
    @Autowired
    private RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, String> tempPasswords = new ConcurrentHashMap<>();

    @PostMapping("/json")
    public ResponseEntity<String> uploadJson(@RequestParam("file") MultipartFile file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<UserEntity> users = objectMapper.readValue(file.getInputStream(), objectMapper.getTypeFactory().constructCollectionType(List.class, UserEntity.class));
            processUsers(users);
            return ResponseEntity.ok("JSON file processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing JSON file: " + e.getMessage());
        }
    }

    @PostMapping("/excel")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            List<UserEntity> users = new ArrayList<>();
            rowIterator.next(); // Skip header
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                UserEntity user = new UserEntity();
                user.setUserId(row.getCell(0).getStringCellValue());
                user.setEmployeeId(row.getCell(1).getStringCellValue());
                user.setFirstName(row.getCell(2).getStringCellValue());
                user.setLastName(row.getCell(3).getStringCellValue());
                user.setEmail(row.getCell(4).getStringCellValue());
                user.setPhone(row.getCell(5).getStringCellValue());
                user.setGender(row.getCell(7).getStringCellValue());
                user.setActive((int) row.getCell(8).getNumericCellValue());
                users.add(user);
            }
            processUsers(users);
            return ResponseEntity.ok("Excel file processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing Excel file: " + e.getMessage());
        }
    }

    @PostMapping("/csv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            List<UserEntity> users = parseCsv(file);
            processUsers(users);
            return ResponseEntity.ok("CSV file processed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing CSV file: " + e.getMessage());
        }
    }

    @Async
    @Transactional
    public void processUsers(List<UserEntity> users) {
        ModelMapper mapper = new ModelMapper();
        for (UserEntity user : users) {
            Optional<UserEntity> existingUser = Optional.ofNullable(userRepository.findByEmail(user.getEmail()));
            if (existingUser.isPresent()) {
                UserEntity dbUser = existingUser.get();
                dbUser.setFirstName(user.getFirstName());
                dbUser.setLastName(user.getLastName());
                dbUser.setPhone(user.getPhone());
                dbUser.setGender(user.getGender());
                dbUser.setActive(user.getActive());

                dbUser.setSections(user.getSections());
                dbUser.setRoles(user.getRoles());

                UserEntity save = userRepository.save(dbUser);

            } else {
                String tempPassword = UUID.randomUUID().toString();
                tempPasswords.put(user.getEmail(), passwordEncoder.encode(tempPassword));
                user.setEncryptedPassword(tempPassword); // Store unencrypted password in DB
                userRepository.save(user);
            }
        }
    }

    private List<UserEntity> parseCsv(MultipartFile file) throws Exception {
        List<UserEntity> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            Iterator<CSVRecord> iterator = csvParser.iterator();

            if (iterator.hasNext()) {
                iterator.next(); // Skip the first row (assuming it's a header)
            }

            while (iterator.hasNext()) {

                CSVRecord record = iterator.next();
                UserEntity user = new UserEntity();

                String[] nameParts = record.get(1).split(" ", 2);
                String firstName = nameParts[0]; // First name
                String lastName = nameParts.length > 1 ? nameParts[1] : ""; // Last name (handle cases with only one name)

                user.setUserId(utils.generateUserId(10));

                user.setEmployeeId(record.get(7));
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(record.get(8));

                user.setPhone(record.get(20));
                user.setGender(record.get(19));

                String roleString = record.get(9).toUpperCase(); // Read roles from CSV
                String[] roleArray = roleString.split(","); // Split roles by comma

                List<RoleEntity> roleEntities = new ArrayList<>();

                for (String role : roleArray) {
                    Roles mappedRole;
                    try {
                        mappedRole = Roles.fromString(role.trim()); // Convert to enum
                    } catch (IllegalArgumentException e) {
                        System.out.println("Unknown role found: " + role.trim());
                        continue; // Skip unknown roles
                    }

                    RoleEntity byName = roleRepository.findByName(mappedRole.name());
                    if (byName != null) {
                        roleEntities.add(byName);
                    } else {
                        System.out.println("Role not found in database: " + mappedRole.name());
                    }
                }
                user.setRoles(roleEntities);

                List<SectionEntity> sectionEntities = new ArrayList<>();
                String section_  = record.get(21);
                SectionEntity section = userService.getSection(section_);
                if(section == null)
                    section = userService.createSection(section_);

                sectionEntities.add(section);
                user.setSections(sectionEntities);

                users.add(user);
            }
        }
        return users;
    }
}

