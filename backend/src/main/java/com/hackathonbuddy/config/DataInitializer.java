package com.hackathonbuddy.config;

import com.hackathonbuddy.entity.Hackathon;
import com.hackathonbuddy.entity.Role;
import com.hackathonbuddy.entity.Skill;
import com.hackathonbuddy.entity.User;
import com.hackathonbuddy.entity.UserSkill;
import com.hackathonbuddy.repository.HackathonRepository;
import com.hackathonbuddy.repository.RoleRepository;
import com.hackathonbuddy.repository.SkillRepository;
import com.hackathonbuddy.repository.UserRepository;
import com.hackathonbuddy.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import com.hackathonbuddy.entity.Project;
import com.hackathonbuddy.entity.ProjectTask;
import com.hackathonbuddy.repository.ProjectRepository;
import com.hackathonbuddy.repository.ProjectTaskRepository;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final HackathonRepository hackathonRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initSkills();
        initHackathons();
        initAdminUser();
        initSampleUsers();
        initGoogleSheetUsers();
        initSampleProjects();
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            List<Role> roles = Arrays.asList(
                    Role.builder().name("STUDENT").build(),
                    Role.builder().name("DEVELOPER").build(),
                    Role.builder().name("ORGANIZER").build(),
                    Role.builder().name("COMPANY").build(),
                    Role.builder().name("ADMIN").build(),
                    Role.builder().name("HACKATHON_ADMIN").build(),
                    Role.builder().name("DEVELOPER_ADMIN").build(),
                    Role.builder().name("SUPER_ADMIN").build()
            );
            roleRepository.saveAll(roles);
            log.info("Initialized {} roles", roles.size());
        } else {
            // Ensure new roles exist even if roles table is already populated
            if (roleRepository.findByName("HACKATHON_ADMIN").isEmpty()) {
                roleRepository.save(Role.builder().name("HACKATHON_ADMIN").build());
                log.info("Added HACKATHON_ADMIN role");
            }
            if (roleRepository.findByName("SUPER_ADMIN").isEmpty()) {
                roleRepository.save(Role.builder().name("SUPER_ADMIN").build());
                log.info("Added SUPER_ADMIN role");
            }
            if (roleRepository.findByName("DEVELOPER_ADMIN").isEmpty()) {
                roleRepository.save(Role.builder().name("DEVELOPER_ADMIN").build());
                log.info("Added DEVELOPER_ADMIN role");
            }
        }
    }

    private void initSkills() {
        if (skillRepository.count() == 0) {
            List<Skill> skills = Arrays.asList(
                    Skill.builder().name("React").category("Frontend").build(),
                    Skill.builder().name("Vue.js").category("Frontend").build(),
                    Skill.builder().name("Angular").category("Frontend").build(),
                    Skill.builder().name("Spring Boot").category("Backend").build(),
                    Skill.builder().name("Node.js").category("Backend").build(),
                    Skill.builder().name("Django").category("Backend").build(),
                    Skill.builder().name("Python").category("ML").build(),
                    Skill.builder().name("TensorFlow").category("ML").build(),
                    Skill.builder().name("PyTorch").category("ML").build(),
                    Skill.builder().name("Docker").category("DevOps").build(),
                    Skill.builder().name("Kubernetes").category("DevOps").build(),
                    Skill.builder().name("AWS").category("DevOps").build(),
                    Skill.builder().name("Figma").category("Design").build(),
                    Skill.builder().name("MySQL").category("Database").build(),
                    Skill.builder().name("MongoDB").category("Database").build(),
                    Skill.builder().name("PostgreSQL").category("Database").build(),
                    Skill.builder().name("Java").category("Backend").build(),
                    Skill.builder().name("JavaScript").category("Frontend").build(),
                    Skill.builder().name("TypeScript").category("Frontend").build(),
                    Skill.builder().name("Solidity").category("Blockchain").build(),
                    Skill.builder().name("Flutter").category("Mobile").build(),
                    Skill.builder().name("FastAPI").category("Backend").build(),
                    Skill.builder().name("NLP").category("ML").build(),
                    Skill.builder().name("CI/CD").category("DevOps").build()
            );
            skillRepository.saveAll(skills);
            log.info("Initialized {} skills", skills.size());
        }
    }

    private void initHackathons() {
        if (hackathonRepository.count() == 0) {
            List<Hackathon> hackathons = Arrays.asList(
                    Hackathon.builder()
                            .title("AI Innovation Challenge 2026")
                            .description("Build innovative AI-powered solutions that solve real-world problems using machine learning and generative AI.")
                            .category("AI/ML")
                            .organizer("Google Developers")
                            .startDate(LocalDate.of(2026, 8, 18))
                            .endDate(LocalDate.of(2026, 8, 20))
                            .registrationDeadline(LocalDate.of(2026, 8, 15))
                            .prizePool("₹5,00,000")
                            .location("Online")
                            .mode("ONLINE")
                            .minTeamSize(2).maxTeamSize(4)
                            .duration("48 Hours").icon("🤖").color("purple").type("ai")
                            .level("Intermediate").status("Open").participantCount(1240)
                            .isActive(true).build(),
                    Hackathon.builder()
                            .title("Smart City Hackathon")
                            .description("Create technology solutions for smarter transportation, sustainable cities and better public services.")
                            .category("Smart City")
                            .organizer("Municipal Corporation of India")
                            .startDate(LocalDate.of(2026, 8, 25))
                            .endDate(LocalDate.of(2026, 8, 27))
                            .registrationDeadline(LocalDate.of(2026, 8, 22))
                            .prizePool("₹3,00,000")
                            .location("Mumbai")
                            .mode("OFFLINE")
                            .minTeamSize(2).maxTeamSize(5)
                            .duration("36 Hours").icon("🏙️").color("blue").type("city")
                            .level("Intermediate").status("Open").participantCount(860)
                            .isActive(true).build(),
                    Hackathon.builder()
                            .title("FinTech Challenge")
                            .description("Build the next generation of financial technology products with secure and scalable solutions.")
                            .category("FinTech")
                            .organizer("RazorPay & PayTM")
                            .startDate(LocalDate.of(2026, 9, 2))
                            .endDate(LocalDate.of(2026, 9, 4))
                            .registrationDeadline(LocalDate.of(2026, 8, 29))
                            .prizePool("₹4,00,000")
                            .location("Bangalore")
                            .mode("HYBRID")
                            .minTeamSize(2).maxTeamSize(4)
                            .duration("48 Hours").icon("💳").color("orange").type("fintech")
                            .level("Advanced").status("Open").participantCount(720)
                            .isActive(true).build(),
                    Hackathon.builder()
                            .title("Web3 Builders Arena")
                            .description("Build decentralized applications and explore the future of blockchain technology.")
                            .category("Web3")
                            .organizer("Ethereum Foundation")
                            .startDate(LocalDate.of(2026, 9, 10))
                            .endDate(LocalDate.of(2026, 9, 12))
                            .registrationDeadline(LocalDate.of(2026, 9, 6))
                            .prizePool("₹2,50,000")
                            .location("Online")
                            .mode("ONLINE")
                            .minTeamSize(1).maxTeamSize(3)
                            .duration("48 Hours").icon("⛓️").color("cyan").type("web3")
                            .level("Advanced").status("Open").participantCount(530)
                            .isActive(true).build(),
                    Hackathon.builder()
                            .title("GreenTech Innovation Hack")
                            .description("Develop technology-driven solutions for climate change, renewable energy and sustainability.")
                            .category("Environment")
                            .organizer("GreenTech Foundation")
                            .startDate(LocalDate.of(2026, 9, 18))
                            .endDate(LocalDate.of(2026, 9, 19))
                            .registrationDeadline(LocalDate.of(2026, 9, 14))
                            .prizePool("₹2,00,000")
                            .location("Pune")
                            .mode("OFFLINE")
                            .minTeamSize(2).maxTeamSize(5)
                            .duration("24 Hours").icon("🌱").color("green").type("green")
                            .level("Beginner").status("Open").participantCount(430)
                            .isActive(true).build(),
                    Hackathon.builder()
                            .title("Healthcare AI Sprint")
                            .description("Use AI and software technology to create better healthcare experiences and intelligent solutions.")
                            .category("Healthcare")
                            .organizer("Apollo Hospitals")
                            .startDate(LocalDate.of(2026, 9, 25))
                            .endDate(LocalDate.of(2026, 9, 27))
                            .registrationDeadline(LocalDate.of(2026, 9, 21))
                            .prizePool("₹3,50,000")
                            .location("Online")
                            .mode("ONLINE")
                            .minTeamSize(2).maxTeamSize(4)
                            .duration("48 Hours").icon("🏥").color("pink").type("health")
                            .level("Intermediate").status("Open").participantCount(650)
                            .isActive(true).build()
            );
            hackathonRepository.saveAll(hackathons);
            log.info("Initialized {} sample hackathons", hackathons.size());
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail("admin@hackathonbuddy.com")) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            User admin = User.builder()
                    .firstName("System")
                    .lastName("Administrator")
                    .email("admin@hackathonbuddy.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(adminRole)
                    .primaryRole("Platform Administrator")
                    .location("HQ Command Center")
                    .bio("Platform Administrator with full permissions over hackathon challenges, user moderation, and AI telemetry.")
                    .isActive(true)
                    .profileComplete(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: admin@hackathonbuddy.com / admin123");
        }

        // Create Super Admin user
        if (!userRepository.existsByEmail("superadmin@hackathonbuddy.com")) {
            Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                    .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

            User superAdmin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("superadmin@hackathonbuddy.com")
                    .password(passwordEncoder.encode("superadmin123"))
                    .role(superAdminRole)
                    .primaryRole("Main Developer / System Owner")
                    .location("Developer HQ")
                    .bio("Main developer with complete system control. Manages credentials, roles, and access for all administrators.")
                    .isActive(true)
                    .profileComplete(true)
                    .build();
            userRepository.save(superAdmin);
            log.info("Super Admin user created: superadmin@hackathonbuddy.com / superadmin123");
        }

        // Create Hackathon Admin user
        if (!userRepository.existsByEmail("hackadmin@hackathonbuddy.com")) {
            Role hackathonAdminRole = roleRepository.findByName("HACKATHON_ADMIN")
                    .orElseThrow(() -> new RuntimeException("HACKATHON_ADMIN role not found"));

            User hackathonAdmin = User.builder()
                    .firstName("Hackathon")
                    .lastName("Organizer")
                    .email("hackadmin@hackathonbuddy.com")
                    .password(passwordEncoder.encode("hackadmin123"))
                    .role(hackathonAdminRole)
                    .primaryRole("Hackathon Organizer")
                    .location("Event Management Center")
                    .bio("Hackathon organizer responsible for managing events, circulating information, and coordinating with participants.")
                    .isActive(true)
                    .profileComplete(true)
                    .build();
            userRepository.save(hackathonAdmin);
            log.info("Hackathon Admin user created: hackadmin@hackathonbuddy.com / hackadmin123");
        }

        // Create Developer Admin user
        if (!userRepository.existsByEmail("devadmin@hackathonbuddy.com")) {
            Role devAdminRole = roleRepository.findByName("DEVELOPER_ADMIN")
                    .orElseThrow(() -> new RuntimeException("DEVELOPER_ADMIN role not found"));

            User devAdmin = User.builder()
                    .firstName("Developer")
                    .lastName("Admin")
                    .email("devadmin@hackathonbuddy.com")
                    .password(passwordEncoder.encode("devadmin123"))
                    .role(devAdminRole)
                    .primaryRole("Developer Manager")
                    .location("Tech Hub")
                    .bio("Developer Admin responsible for managing developer profiles, technical resources, and developer activity on the platform.")
                    .isActive(true)
                    .profileComplete(true)
                    .build();
            userRepository.save(devAdmin);
            log.info("Developer Admin user created: devadmin@hackathonbuddy.com / devadmin123");
        }
    }

    private void initSampleUsers() {
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

        if (!userRepository.existsByEmail("sanika@example.com")) {
            User sanika = User.builder()
                    .firstName("Sanika").lastName("Haridas Pandhare")
                    .email("sanika@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(studentRole)
                    .primaryRole("Full Stack Developer")
                    .location("Pune, India")
                    .bio("Full Stack Developer passionate about building high-impact web apps, AI integrations, and winning hackathons.")
                    .githubUrl("https://github.com/sanikapandhare")
                    .linkedinUrl("https://linkedin.com/in/sanikapandhare")
                    .isActive(true).profileComplete(true)
                    .build();
            userRepository.save(sanika);
            log.info("Sample user created: sanika@example.com / password123");
        }

        if (!userRepository.existsByEmail("priya@example.com")) {
            userRepository.save(User.builder()
                    .firstName("Priya").lastName("Sharma")
                    .email("priya@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(studentRole)
                    .primaryRole("UI/UX Designer")
                    .location("Mumbai, India")
                    .isActive(true).profileComplete(true)
                    .build());
        }

        if (!userRepository.existsByEmail("rohan@example.com")) {
            userRepository.save(User.builder()
                    .firstName("Rohan").lastName("Mehta")
                    .email("rohan@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(studentRole)
                    .primaryRole("ML Engineer")
                    .location("Delhi, India")
                    .isActive(true).profileComplete(true)
                    .build());
        }

        if (!userRepository.existsByEmail("aman@example.com")) {
            userRepository.save(User.builder()
                    .firstName("Aman").lastName("Khan")
                    .email("aman@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(studentRole)
                    .primaryRole("DevOps Engineer")
                    .location("Bangalore, India")
                    .isActive(true).profileComplete(true)
                    .build());
        }

        // Ensure sample users have their saved skills in user_skills table
        userRepository.findByEmail("sanika@example.com").ifPresent(u ->
                assignSkillsToUserIfMissing(u, List.of("React", "Spring Boot", "MySQL", "JavaScript")));
        userRepository.findByEmail("priya@example.com").ifPresent(u ->
                assignSkillsToUserIfMissing(u, List.of("Figma", "UI/UX", "Tailwind CSS", "Design Systems")));
        userRepository.findByEmail("rohan@example.com").ifPresent(u ->
                assignSkillsToUserIfMissing(u, List.of("Python", "TensorFlow", "PyTorch", "NLP", "Machine Learning")));
        userRepository.findByEmail("aman@example.com").ifPresent(u ->
                assignSkillsToUserIfMissing(u, List.of("AWS", "Docker", "Kubernetes", "CI/CD", "Linux")));
    }

    private void assignSkillsToUserIfMissing(User user, List<String> skillNames) {
        if (userSkillRepository.findByUser(user).isEmpty()) {
            for (String sName : skillNames) {
                final String sNameFinal = sName;
                Skill skill = skillRepository.findByNameIgnoreCase(sNameFinal)
                        .orElseGet(() -> skillRepository.save(Skill.builder().name(sNameFinal).category("General").build()));
                userSkillRepository.save(UserSkill.builder()
                        .user(user)
                        .skill(skill)
                        .proficiencyLevel(4)
                        .build());
            }
            log.info("Assigned initial skills {} to {}", skillNames, user.getEmail());
        }
    }

    private void initSampleProjects() {
        if (projectRepository.count() == 0) {
            User owner = userRepository.findByEmail("sanika@example.com")
                    .or(() -> userRepository.findAll().stream().findFirst())
                    .orElse(null);

            Project p1 = projectRepository.save(Project.builder()
                    .name("AI Study Assistant")
                    .domain("AI / Education")
                    .status("Active")
                    .description("An AI-powered study assistant that helps students create personalized learning plans, summarize lectures, and improve exam productivity.")
                    .technologies("React, Node.js, Python, Machine Learning")
                    .members(4)
                    .progress(72)
                    .user(owner)
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build());

            projectTaskRepository.saveAll(Arrays.asList(
                    ProjectTask.builder()
                            .project(p1)
                            .title("Design login and onboarding screen")
                            .description("Create responsive layout and authentication flows.")
                            .assignedTo("Priya")
                            .priority("High")
                            .status("Completed")
                            .build(),
                    ProjectTask.builder()
                            .project(p1)
                            .title("Build authentication and session API")
                            .description("Implement JWT auth endpoints and input validation.")
                            .assignedTo("Rohan")
                            .priority("High")
                            .status("In Progress")
                            .build(),
                    ProjectTask.builder()
                            .project(p1)
                            .title("Create teammate recommendation model")
                            .description("Develop vector similarity and skill matching algorithms.")
                            .assignedTo("Aman")
                            .priority("Medium")
                            .status("To Do")
                            .build()
            ));

            Project p2 = projectRepository.save(Project.builder()
                    .name("Smart Healthcare Platform")
                    .domain("Healthcare / AI")
                    .status("Active")
                    .description("A secure digital health ecosystem that connects patients, doctors, hospitals and ML diagnostic services.")
                    .technologies("React, Spring Boot, PostgreSQL, Python")
                    .members(4)
                    .progress(45)
                    .user(owner)
                    .createdAt(LocalDateTime.now().minusDays(8))
                    .build());

            projectTaskRepository.saveAll(Arrays.asList(
                    ProjectTask.builder()
                            .project(p2)
                            .title("Design patient portal UI")
                            .description("Build appointments and doctor dashboard views.")
                            .assignedTo("Priya")
                            .priority("Medium")
                            .status("Completed")
                            .build(),
                    ProjectTask.builder()
                            .project(p2)
                            .title("Implement EHR synchronization")
                            .description("Connect health records securely with Spring Boot API.")
                            .assignedTo("Sanika")
                            .priority("High")
                            .status("In Progress")
                            .build()
            ));

            Project p3 = projectRepository.save(Project.builder()
                    .name("FinTech Smart Ledger")
                    .domain("FinTech / Web3")
                    .status("Active")
                    .description("Automated reconciliation and fraud prevention engine for high-frequency digital payments.")
                    .technologies("React, FastAPI, PostgreSQL, Docker")
                    .members(3)
                    .progress(60)
                    .user(owner)
                    .createdAt(LocalDateTime.now().minusDays(12))
                    .build());

            projectTaskRepository.saveAll(Arrays.asList(
                    ProjectTask.builder()
                            .project(p3)
                            .title("Smart contract audit and test suite")
                            .description("Run security verification tests on token distribution.")
                            .assignedTo("Rohan")
                            .priority("High")
                            .status("Completed")
                            .build()
            ));

            log.info("Initialized {} starter projects with tasks", projectRepository.count());
        }
    }

    private void initGoogleSheetUsers() {
        String[][] sheetUsers = {
            {"sujalbhopale@gmail.com", "Sujal", "Bhopale", "SUPER_ADMIN", "System Owner & Super Admin", "Ichalkaranji, Maharashtra", "8010038042", "I am an Aeronautical Engineering student with skills in FEA, CAD modeling, ANSYS Workbench, HyperMesh meshing, composite material analysis, aircraft structures, and engineering simulations. I am seeking opportunities in aerospace design, analysis, simulation, manufacturing, and research & development."},
            {"adityanikam234@gmail.com", "Aditya", "Nikam", "SUPER_ADMIN", "System Owner & Super Admin", "Kolhapur", "9168354484", "CFD, CAD Modeling, UAV, Propulsion, Aerodynamics"},
            {"manerajwardhan68@gmail.com", "Rajwardhan", "Mane", "ADMIN", "Platform Administrator", "Palus", "9834706019", "Aircraft maintenance and unmaned air vehicle"},
            {"23071007_aero@adcet.in", "Amarnath", "Kamble", "ADMIN", "Platform Administrator", "Sangli", "8208269680", "UAV & Flight Systems, CFD"},
            {"rautshwta161205@gmail.com", "Shweta", "Raut", "ADMIN", "Platform Administrator", "Pune", "7822916824", "My area of specialization is Aeronautical Engineering with a focus on Aircraft Structures, Aircraft Systems Design, Finite Element Analysis (FEA), Structural Analysis and Engineering Simulations. I have experience with ANSYS, HyperMesh , Abaqus and SolidWorks for modeling, analysis and design validation, with a strong interest in aerospace structural design and analysis."},
            {"vedantigondhali@gmail.com", "Vedanti", "Gondhali", "HACKATHON_ADMIN", "Hackathon Organizer & Manager", "Thane, Maharashtra.", "9867980126", "Area of Specialization - 1. Aerospace and Aeronautical Engineering 2. Computational Fluid Dynamics (CFD 3. Airport Operations and Aviation Technology 4. Minor Mechanical Engineering / Heat Transfer / Hydrogen Internal Combustion Engines (HICE). Skills - Aircraft Design, ANSYS Fluent, ANSYS Workbench, ANSYS Forte, HyperWorks, Solidworks, MATLAB, Python, C++, CAD Modelling, Aerodynamic Performance Analysis, Wind Tunnel Testing, Airfoil Analysis. Soft Skills - Teamwork, Communication, Leadership, Management, Problem Solving"},
            {"mohanishp65@gmail.com", "Mohanish", "Patil", "HACKATHON_ADMIN", "Hackathon Organizer & Manager", "Bhusawal", "9960885619", "Designing"},
            {"apurvadawane26@gmail.com", "Apurva", "Dawane", "HACKATHON_ADMIN", "Hackathon Organizer & Manager", "Vairag", "8830993594", "UAV and Drone Technology , Flight Control Systems ,Natural Fiber Composite Development, Aircraft Design and Performance Analysis"},
            {"shreyaganbavale1@gmail.com", "Shreya", "Ganbavale", "HACKATHON_ADMIN", "Hackathon Organizer & Manager", "Kolhapur", "8080428571", "Area of Specialization: Computational Fluid Dynamics (CFD) Skill Sets: ANSYS Fluent, HyperWorks, SolidWorks, AutoCAD, Aerodynamic Analysis, Flow Simulation, Engineering Design and Technical Problem Solving."},
            {"saravadepramila@gmail.com", "Pramila", "Saravade", "DEVELOPER_ADMIN", "Developer Community Manager", "KAVATHE MAHANKAL", "8999699323", "Aircraft maintenance"},
            {"aznaanshaikh2005@gmail.com", "Aznaan", "Shaikh", "DEVELOPER_ADMIN", "Developer Community Manager", "Bhiwandi", "8793777579", "CAD Design (SolidWorks, AutoCAD), Structural Analysis (ANSYS), Flight Simulation (STK), and UAV Systems (Mission Planner)"},
            {"arfarihantodalbag@gmail.com", "Arfa", "Todalbag", "DEVELOPER_ADMIN", "Developer Community Manager", "Miraj", "7559380345", "Unmanned Arial Vehicles"},
            {"mhetremrudula1735@gmail.com", "Mrudula", "Mhetre", "DEVELOPER_ADMIN", "Developer Community Manager", "Sangli", "9022194072", "Propulsion, Aerodynamics, Structures, Designs (Softwares), Maintenance, Materials and Compostites, Sales, IoT (Sensors and Actuators), Drones and Rockets, Reserch and development, Manufacturing and Production."},
            {"chavankartik7620@gmail.com", "Kartik", "Chavan", "ORGANIZER", "Event Organizer", "Sangola", "7620128149", "Aeronautical , CSE ( minor)"},
            {"thetemanav347@gmail.com", "Manav", "Thete", "ORGANIZER", "Event Organizer", "JALNA", "9322807649", "Computataional Fluid dynamics, CAD, Ansys Fluent, Ansys Forte"},
            {"shubhamvagare5@gmail.com", "Shubham", "Vagare", "ORGANIZER", "Event Organizer", "Solapur", "9284763425", "Python, React, Aerospace, Engineering"},
            {"shewaleshivaji57@gmail.com", "Shivaji", "Shewale", "ORGANIZER", "Event Organizer", "Kolhapur", "7249775149", "UAV"},
            {"sarthakdeshmane6132@gmail.com", "Sarthak", "Deshmane", "COMPANY", "Industry Partner / Recruiter", "Pandharpur", "9022500873", "1. Structural Analysis and Finite Element Analysis (FEA) 2. Aerospace Structures and Engineering Design 3. Computer-Aided Design (CAD) 4. Simulation and Modeling of Engineering Systems"},
            {"gaurishankarkudakale@gmail.com", "GOURISHANKAR", "KUDKALE", "COMPANY", "Industry Partner / Recruiter", "Ashta Kasar", "7588526305", "CFD"},
            {"prathmesh6499@gmail.com", "Prathmesh", "Patil", "COMPANY", "Industry Partner / Recruiter", "Kolhapur", "8484984499", "B.Tech Aronautical enginnering with honours in Space Technology & minors in EV. Specializations are structural analysis, FEA, Flow analysis and Experimental analysis."},
            {"bashingekedar@gmail.com", "Kedar", "Bashinge", "COMPANY", "Industry Partner / Recruiter", "Solapur", "8788953074", "My area of specialization is Aeronautical Engineering. I have knowledge of aircraft systems, aerodynamics, propulsion, and drone technology. I also have basic skills in CAD modeling, ANSYS, MATLAB, Python, and database management. I am interested in opportunities related to aerospace, design, analysis, manufacturing, and technical support."},
            {"jadhavnakul026@gmail.com", "NAKUL", "JADHAV", "DEVELOPER", "Full Stack Developer", "Chhatrapati Sambhajinagar", "8605255843", "1. Cad Softwares: Solidworks, Autocad 2. Meshing Software: Hypermesh 3. Simulation Software: Ansys ( Structural, Fluent) 4. Programming Languages : Python,C,C++,Matlab"},
            {"akashpatil121010@gmail.com", "Akash", "Patil", "DEVELOPER", "Full Stack Developer", "Tasgaon", "9322527899", "I am expertise in UAV systems, drone operations, aircraft design, remote sensing, GIS, GNSS/RTK surveying, and aerial mapping. Strong foundation in aerodynamics, propulsion, thermodynamics, aircraft performance, and flight dynamics. Proficient in ANSYS Fluent, SolidWorks, QGIS, Mission Planner, MATLAB, and Python for engineering analysis, CFD simulation, geospatial data processing, and mission planning. Experienced in VTOL aircraft design, aerodynamic optimization, and satellite-based environmental monitoring projects, with a passion for advancing aerospace and geospatial technologies"},
            {"dudhalsaniya@gmail.com", "Saniya", "Dudhal", "DEVELOPER", "Full Stack Developer", "Phaltan", "9529871411", "CFD, 3D Designing, Ansys fluent, solidworks"},
            {"moreabhishek1235@gmai.com", "Abhishek", "More", "DEVELOPER", "Full Stack Developer", "Angar", "8261976570", "UAV, SOLID WORKS"},
            {"baitsoham03@gmail.com", "Soham", "Bait", "DEVELOPER", "Full Stack Developer", "Nalasopara", "7498992453", "CAD design and manufacturing"},
            {"adityaborde03@gmail.com", "Aditya", "Borde", "DEVELOPER", "Full Stack Developer", "Jafrabad", "9823856646", "Unmanned Aerial Vehicle (UAV), CAD & Design, Computational Fluid Dynamics"},
            {"guravprachi131@gmail.com", "Prachi", "Gurav", "STUDENT", "Student Innovator", "Kolhapur", "7499503527", "Computational fluid dynamics"},
            {"shrutijangam111@gmail.com", "Shrutika", "Jangam", "STUDENT", "Student Innovator", "Kolhapur", "9175771931", "Aircrafts structural analysis"},
            {"abhishekshendes1919@gmail.com", "Abhishek", "Shende", "STUDENT", "Student Innovator", "Hinganghat", "8308006640", "Aircraft Maintenance ."},
            {"omkarchaundkar6@gmail.com", "Omkar", "Chaundkar", "STUDENT", "Student Innovator", "Naygaon", "7666348187", "Python, React, Aerospace, Engineering"},
            {"prajaktacdethe@gmail.com", "Prajakta", "Dethe", "STUDENT", "Student Innovator", "Kopargaon", "9082270354", "Computational Fluid Dynamics (CFD) Aeronautical Engineering Aerodynamic Analysis and Simulation Engineering Design and CAD Modeling (SolidWorks) ANSYS Fluent and ANSYS Workbench Aircraft Structures and Airframe Design Python Programming for Engineering Applications Technical Documentation"},
            {"balajisawalkar318@gmail.com", "Balaji", "Sawalkar", "STUDENT", "Student Innovator", "Dharashiv", "9834633077", "Solidworks-CAD modelling,UAV, Basic Ansys simulation."},
            {"ayush.chougule3june@gmail.com", "Ayush", "Chougule", "STUDENT", "Student Innovator", "Kolhapur", "9699927544", "Maintainance"},
            {"salim9844354536@gmail.com", "Siraj", "Shaikh", "STUDENT", "Student Innovator", "Miraj", "9844354536", "Design Engineering (Aircraft & Mechanical Design), CAD Modeling using SolidWorks Python Programming, and AI/ML Applications."},
            {"omjadhav2705@gmail.com", "Om", "Jadhav", "STUDENT", "Student Innovator", "Tasgaon", "7972075476", "Aircraft and Airframe Maintenance, solidworks"},
            {"psp.pranav009@gmail.com", "Pranav", "Patil", "STUDENT", "Student Innovator", "Karad", "7249531656", "Basic knowledge of aircraft systems (propulsion, structures, avionics) with focus on maintenance practices, inspection, and troubleshooting. Hands-on exposure through internships, with a foundation in mechanical engineering concepts and familiarity with safety procedures and engineering drawings."},
            {"moinmulla502@gmail.com", "Moin", "Mulla", "STUDENT", "Student Innovator", "Bavachi", "9960988573", "Aerospace, Aeronautical, Mechanical"},
            {"shivanshmali18@gmail.com", "Sonam", "Mali", "STUDENT", "Student Innovator", "Bavachi", "9673582132", "Area of Specialization: Computational Fluid Dynamics (CFD) Skill Set - CFD analysis using ANSYS Fluent.Geometry creation and Meshing in ANSYS Workbench.Fluid Flow and Heat Transfer Analysis.Multiphase Flow Simulation.Turbulence Modeling.Post processing and result Interpretation."},
            {"shwetajadhav1021@gmail.com", "Shweta", "Jadhav", "STUDENT", "Student Innovator", "Kolhapur", "8329040721", "Aeronautical Engineering, CFD Analysis, ANSYS Fluent, ANSYS Workbench, SolidWorks, Python Programming, Basic C,C++, CAD Modelling, Fluid Mechanics."},
            {"prajyotpatil2731@gmail.com", "Prajyot", "Patil", "STUDENT", "Student Innovator", "Ashta", "7083912925", "Unmanned aerial vehicles"},
        };

        for (String[] u : sheetUsers) {
            String email = u[0];
            if (!userRepository.existsByEmail(email)) {
                Role role = roleRepository.findByName(u[3]).orElse(null);
                if (role != null) {
                    User user = User.builder()
                            .email(email)
                            .firstName(u[1])
                            .lastName(u[2])
                            .password(passwordEncoder.encode("password"))
                            .role(role)
                            .primaryRole(u[4])
                            .location(u[5])
                            .phone(u[6])
                            .bio(u[4] + " with a focus in Aeronautical Engineering. Skills: " + u[7])
                            .isActive(true)
                            .profileComplete(true)
                            .build();
                    userRepository.save(user);
                }
            }
        }
        log.info("Initialized {} Google Sheet users", sheetUsers.length);
    }
}
