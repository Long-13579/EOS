package com.ces.eos.seed;

import com.ces.eos.dto.request.CreateL10MeetingRequest;
import com.ces.eos.entity.CustomYear;
import com.ces.eos.entity.Headline;
import com.ces.eos.entity.Issue;
import com.ces.eos.entity.IssueType;
import com.ces.eos.entity.L10Meeting;
import com.ces.eos.entity.Metric;
import com.ces.eos.entity.MetricValue;
import com.ces.eos.entity.Quarter;
import com.ces.eos.entity.Rock;
import com.ces.eos.entity.Role;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.Todo;
import com.ces.eos.entity.User;
import com.ces.eos.entity.Week;
import com.ces.eos.enums.L10MeetingStatus;
import com.ces.eos.enums.MetricOperator;
import com.ces.eos.enums.MetricUnit;
import com.ces.eos.enums.RockCategory;
import com.ces.eos.enums.RockStatus;
import com.ces.eos.enums.TodoStatus;
import com.ces.eos.enums.UserRole;
import com.ces.eos.repository.HeadlineRepository;
import com.ces.eos.repository.IssueRepository;
import com.ces.eos.repository.IssueTypeRepository;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.repository.MetricRepository;
import com.ces.eos.repository.MetricValueRepository;
import com.ces.eos.repository.QuarterRepository;
import com.ces.eos.repository.RockRepository;
import com.ces.eos.repository.RoleRepository;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.repository.TodoRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.repository.WeekRepository;
import com.ces.eos.repository.YearRepository;
import com.ces.eos.service.L10MeetingService;
import com.ces.eos.util.DateUtils;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@Order(100)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final RockRepository rockRepository;
  private final MetricRepository metricRepository;
  private final MetricValueRepository metricValueRepository;
  private final HeadlineRepository headlineRepository;
  private final IssueRepository issueRepository;
  private final IssueTypeRepository issueTypeRepository;
  private final TodoRepository todoRepository;
  private final L10MeetingRepository l10MeetingRepository;
  private final WeekRepository weekRepository;
  private final YearRepository yearRepository;
  private final QuarterRepository quarterRepository;
  private final L10MeetingService l10MeetingService;

  @Override
  @Transactional
  public void run(String... args) {
    if (userRepository.count() > 0) {
      log.info("action=seed.skip reason=dataAlreadyExists");
      return;
    }

    Role adminRole = roleRepository.findByName(UserRole.ADMIN)
        .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
    Role userRole = roleRepository.findByName(UserRole.USER)
        .orElseThrow(() -> new IllegalStateException("USER role not found"));

    Team leadershipTeam = findOrCreateLeadershipTeam();

    List<Team> teams = seedTeams();
    List<User> users = seedUsers(teams, leadershipTeam, adminRole, userRole);
    Map<String, User> userMap = indexUsers(users);

    seedRocks(teams, leadershipTeam, userMap);
    seedMetrics(teams, userMap);
    seedHeadlines(teams, userMap);
    List<Issue> issues = seedIssues(teams, userMap);
    seedTodos(teams, userMap, issues);
    seedMeetings(teams, leadershipTeam);

    log.info("action=seed.complete");
  }

  private Team findOrCreateLeadershipTeam() {
    return teamRepository.findAll().stream()
        .filter(t -> "Leadership team".equals(t.getName()))
        .findFirst()
        .orElseGet(() -> teamRepository.save(
            Team.builder().name("Leadership team").isLeadership(true).timezone("America/New_York").build()));
  }

  private List<Team> seedTeams() {
    List<Team> teams = List.of(
        Team.builder().name("Engineering").isLeadership(false).timezone("America/New_York").build(),
        Team.builder().name("Product").isLeadership(false).timezone("America/New_York").build(),
        Team.builder().name("Sales").isLeadership(false).timezone("America/New_York").build(),
        Team.builder().name("Marketing").isLeadership(false).timezone("America/New_York").build(),
        Team.builder().name("Customer Success").isLeadership(false).timezone("America/New_York").build(),
        Team.builder().name("People Ops").isLeadership(false).timezone("America/New_York").build(),
        Team.builder().name("Finance").isLeadership(false).timezone("America/New_York").build());

    List<Team> saved = teamRepository.saveAll(teams);
    log.info("action=seed.teamsCreated names={}", saved.stream().map(Team::getName).toList());
    return saved;
  }

  private List<User> seedUsers(List<Team> teams, Team leadershipTeam, Role adminRole, Role userRole) {
    Team eng = teams.get(0), prod = teams.get(1), sales = teams.get(2);
    Team mktg = teams.get(3), cs = teams.get(4), people = teams.get(5), fin = teams.get(6);

    List<User> users = new ArrayList<>();
    users.add(saveUser("Sarah", "Chen", "sarah.chen@cloudscale.com", adminRole, Set.of(leadershipTeam)));
    users.add(saveUser("Long", "Nguyen", "leztwaper@gmail.com", adminRole, Set.of(leadershipTeam)));
    users.add(saveUser("Marcus", "Johnson", "marcus.johnson@cloudscale.com", userRole, Set.of(leadershipTeam, eng)));
    users.add(saveUser("Maya", "Patel", "maya.patel@cloudscale.com", userRole, Set.of(leadershipTeam, prod)));
    users.add(saveUser("James", "Wilson", "james.wilson@cloudscale.com", userRole, Set.of(leadershipTeam, sales)));
    users.add(saveUser("David", "Kim", "david.kim@cloudscale.com", userRole, Set.of(eng)));
    users.add(saveUser("Elena", "Rodriguez", "elena.rodriguez@cloudscale.com", userRole, Set.of(eng)));
    users.add(saveUser("Alex", "Thompson", "alex.thompson@cloudscale.com", userRole, Set.of(prod)));
    users.add(saveUser("Olivia", "Martinez", "olivia.martinez@cloudscale.com", userRole, Set.of(mktg)));
    users.add(saveUser("Liam", "O'Brien", "liam.obrien@cloudscale.com", userRole, Set.of(mktg)));
    users.add(saveUser("Sophia", "Lee", "sophia.lee@cloudscale.com", userRole, Set.of(sales)));
    users.add(saveUser("Noah", "Williams", "noah.williams@cloudscale.com", userRole, Set.of(cs)));
    users.add(saveUser("Emma", "Davis", "emma.davis@cloudscale.com", userRole, Set.of(cs)));
    users.add(saveUser("Ryan", "Chen", "ryan.chen@cloudscale.com", userRole, Set.of(people)));
    users.add(saveUser("Isabella", "Garcia", "isabella.garcia@cloudscale.com", userRole, Set.of(fin)));

    log.info("action=seed.usersCreated count={}", users.size());
    return users;
  }

  private User saveUser(String firstName, String lastName, String email, Role role, Set<Team> teams) {
    return userRepository.save(User.builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .role(role)
        .isActive(true)
        .teams(new HashSet<>(teams))
        .build());
  }

  private Map<String, User> indexUsers(List<User> users) {
    Map<String, User> map = new HashMap<>();
    for (User u : users) {
      map.put(u.getFirstName().toLowerCase() + "_" + u.getLastName().toLowerCase(), u);
    }
    return map;
  }

  private void seedRocks(List<Team> teams, Team leadershipTeam, Map<String, User> userMap) {
    int currentYearNum = Year.now().getValue();
    yearRepository.insertIfNotExists(currentYearNum);
    CustomYear year = yearRepository.findByYear(currentYearNum)
        .orElseThrow(() -> new IllegalStateException("Failed to find or create year " + currentYearNum));

    List<Quarter> quarters = quarterRepository.findAllByOrderByNameAsc();
    Quarter currentQuarter = quarters.stream()
        .filter(DateUtils::isCurrentQuarter)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No current quarter found"));

    LocalDate quarterEnd = calculateQuarterEndDate(currentQuarter, currentYearNum);

    User sarah = userMap.get("sarah_chen");
    User marcus = userMap.get("marcus_johnson");
    User maya = userMap.get("maya_patel");

    // Company rocks (on Leadership team)
    List<Rock> leadershipRocks = List.of(
        Rock.builder().team(leadershipTeam).year(year).quarter(currentQuarter)
            .title("Achieve $5M ARR by end of quarter")
            .description("Drive annual recurring revenue to $5M through new customer acquisition and expansion revenue across all departments.")
            .category(RockCategory.COMPANY).status(RockStatus.ON_TRACK).owner(sarah)
            .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(leadershipTeam.getTimezone())).toInstant()).build(),
        Rock.builder().team(leadershipTeam).year(year).quarter(currentQuarter)
            .title("Launch CloudScale Enterprise Plan")
            .description("Release the Enterprise tier with SSO, audit logs, and dedicated support to unlock larger deal sizes.")
            .category(RockCategory.COMPANY).status(RockStatus.ON_TRACK).owner(marcus)
            .dueDate(quarterEnd.minusWeeks(2).atStartOfDay(DateUtils.resolveZoneId(leadershipTeam.getTimezone())).toInstant()).build(),
        Rock.builder().team(leadershipTeam).year(year).quarter(currentQuarter)
            .title("Hire 5 key roles across Engineering and Sales")
            .description("Fill critical open positions including 2 senior engineers, 1 product manager, and 2 sales development reps.")
            .category(RockCategory.COMPANY).status(RockStatus.OFF_TRACK).owner(sarah)
            .dueDate(quarterEnd.minusWeeks(4).atStartOfDay(DateUtils.resolveZoneId(leadershipTeam.getTimezone())).toInstant()).build());

    rockRepository.saveAll(leadershipRocks);

    // Department rocks per team
    Team eng = teams.get(0), prod = teams.get(1), sales = teams.get(2);
    Team mktg = teams.get(3), cs = teams.get(4), people = teams.get(5), fin = teams.get(6);

    User david = userMap.get("david_kim"), elena = userMap.get("elena_rodriguez");
    User alex = userMap.get("alex_thompson");
    User james = userMap.get("james_wilson"), sophia = userMap.get("sophia_lee");
    User olivia = userMap.get("olivia_martinez"), liam = userMap.get("liam_o'brien");
    User noah = userMap.get("noah_williams"), emma = userMap.get("emma_davis");
    User ryan = userMap.get("ryan_chen");
    User isabella = userMap.get("isabella_garcia");

    List<Rock> deptRocks = new ArrayList<>();

    // Engineering rocks
    deptRocks.add(Rock.builder().team(eng).year(year).quarter(currentQuarter)
        .title("Reduce CI pipeline to under 10 minutes")
        .description("Optimize the CI/CD pipeline to reduce build and test times, targeting sub-10 minute feedback loops.")
        .category(RockCategory.DEPARTMENT).status(RockStatus.ON_TRACK).owner(marcus)
        .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(eng.getTimezone())).toInstant()).build());
    deptRocks.add(Rock.builder().team(eng).year(year).quarter(currentQuarter)
        .title("Migrate legacy monolith to microservices (Phase 2)")
        .description("Complete the migration of the billing and notification services from the monolith to independent microservices.")
        .category(RockCategory.DEPARTMENT).status(RockStatus.ON_TRACK).owner(david)
        .dueDate(quarterEnd.minusWeeks(1).atStartOfDay(DateUtils.resolveZoneId(eng.getTimezone())).toInstant()).build());
    deptRocks.add(Rock.builder().team(eng).year(year).quarter(currentQuarter)
        .title("Improve test coverage to 85%")
        .description("Increase automated test coverage across all services, focusing on the billing and notification microservices.")
        .category(RockCategory.INDIVIDUAL).status(RockStatus.ON_TRACK).owner(elena)
        .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(eng.getTimezone())).toInstant()).build());

    // Product rocks
    deptRocks.add(Rock.builder().team(prod).year(year).quarter(currentQuarter)
        .title("Ship dashboard v2 with reporting module")
        .description("Design and ship the next version of the analytics dashboard with custom report builder and export capabilities.")
        .category(RockCategory.DEPARTMENT).status(RockStatus.ON_TRACK).owner(maya)
        .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(prod.getTimezone())).toInstant()).build());
    deptRocks.add(Rock.builder().team(prod).year(year).quarter(currentQuarter)
        .title("Conduct 20 customer discovery interviews")
        .description("Interview 20 customers to validate the pricing and packaging changes planned for Q3.")
        .category(RockCategory.INDIVIDUAL).status(RockStatus.ON_TRACK).owner(alex)
        .dueDate(quarterEnd.minusWeeks(3).atStartOfDay(DateUtils.resolveZoneId(prod.getTimezone())).toInstant()).build());

    // Sales rocks
    deptRocks.add(Rock.builder().team(sales).year(year).quarter(currentQuarter)
        .title("Close 10 enterprise accounts")
        .description("Close 10 new enterprise accounts with minimum $50K ACV each, targeting mid-market SaaS companies.")
        .category(RockCategory.DEPARTMENT).status(RockStatus.ON_TRACK).owner(james)
        .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(sales.getTimezone())).toInstant()).build());
    deptRocks.add(Rock.builder().team(sales).year(year).quarter(currentQuarter)
        .title("Build outbound SDR playbook")
        .description("Create a repeatable outbound sales development playbook including scripts, sequences, and qualification criteria.")
        .category(RockCategory.INDIVIDUAL).status(RockStatus.ON_TRACK).owner(sophia)
        .dueDate(quarterEnd.minusWeeks(2).atStartOfDay(DateUtils.resolveZoneId(sales.getTimezone())).toInstant()).build());

    // Marketing rocks
    deptRocks.add(Rock.builder().team(mktg).year(year).quarter(currentQuarter)
        .title("Launch content marketing program")
        .description("Establish a regular content cadence with 4 blog posts, 2 case studies, and 1 whitepaper per month.")
        .category(RockCategory.DEPARTMENT).status(RockStatus.ON_TRACK).owner(olivia)
        .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(mktg.getTimezone())).toInstant()).build());
    deptRocks.add(Rock.builder().team(mktg).year(year).quarter(currentQuarter)
        .title("Grow newsletter to 5K subscribers")
        .description("Scale the CloudScale monthly newsletter through SEO optimization, lead magnets, and cross-promotion.")
        .category(RockCategory.INDIVIDUAL).status(RockStatus.DEFERRED).owner(liam)
        .dueDate(quarterEnd.minusWeeks(1).atStartOfDay(DateUtils.resolveZoneId(mktg.getTimezone())).toInstant()).build());

    // Customer Success rocks
    deptRocks.add(Rock.builder().team(cs).year(year).quarter(currentQuarter)
        .title("Reduce NPS detractors by 20%")
        .description("Implement a proactive outreach program to identify and address at-risk accounts before they churn.")
        .category(RockCategory.DEPARTMENT).status(RockStatus.ON_TRACK).owner(noah)
        .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(cs.getTimezone())).toInstant()).build());
    deptRocks.add(Rock.builder().team(cs).year(year).quarter(currentQuarter)
        .title("Build customer onboarding academy")
        .description("Create a self-service onboarding academy with video tutorials, documentation, and certification paths.")
        .category(RockCategory.INDIVIDUAL).status(RockStatus.ON_TRACK).owner(emma)
        .dueDate(quarterEnd.minusWeeks(3).atStartOfDay(DateUtils.resolveZoneId(cs.getTimezone())).toInstant()).build());

    // People Ops rocks
    deptRocks.add(Rock.builder().team(people).year(year).quarter(currentQuarter)
        .title("Implement quarterly performance reviews")
        .description("Design and roll out a quarterly performance review process with OKR tracking and 360-degree feedback.")
        .category(RockCategory.DEPARTMENT).status(RockStatus.ON_TRACK).owner(ryan)
        .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(people.getTimezone())).toInstant()).build());

    // Finance rocks
    deptRocks.add(Rock.builder().team(fin).year(year).quarter(currentQuarter)
        .title("Implement real-time cash flow dashboard")
        .description("Build and deploy a real-time cash flow forecasting dashboard to improve financial visibility for leadership.")
        .category(RockCategory.DEPARTMENT).status(RockStatus.ON_TRACK).owner(isabella)
        .dueDate(quarterEnd.atStartOfDay(DateUtils.resolveZoneId(fin.getTimezone())).toInstant()).build());

    rockRepository.saveAll(deptRocks);
    log.info("action=seed.rocksCreated count={}", 1 + deptRocks.size());
  }

  private LocalDate calculateQuarterEndDate(Quarter quarter, int year) {
    MonthDay end = quarter.getEndDate();
    LocalDate endDate = LocalDate.of(year, end.getMonth(), end.getDayOfMonth());
    if (end.getMonth().getValue() < quarter.getStartDate().getMonth().getValue()) {
      endDate = endDate.plusYears(1);
    }
    return endDate;
  }

  private void seedMetrics(List<Team> teams, Map<String, User> userMap) {
    User marcus = userMap.get("marcus_johnson");
    User maya = userMap.get("maya_patel");
    User james = userMap.get("james_wilson");
    User olivia = userMap.get("olivia_martinez");
    User noah = userMap.get("noah_williams");
    User ryan = userMap.get("ryan_chen");
    User isabella = userMap.get("isabella_garcia");

    Team eng = teams.get(0), prod = teams.get(1), sales = teams.get(2);
    Team mktg = teams.get(3), cs = teams.get(4), people = teams.get(5), fin = teams.get(6);

    List<Metric> allMetrics = new ArrayList<>();

    allMetrics.add(Metric.builder().name("Deploy Frequency").goal("10")
        .unit(MetricUnit.NUMBER).operator(MetricOperator.GREATER_THAN).team(eng).owner(marcus).build());
    allMetrics.add(Metric.builder().name("Bug Count (Open)").goal("15")
        .unit(MetricUnit.NUMBER).operator(MetricOperator.LESS_THAN).team(eng).owner(marcus).build());

    allMetrics.add(Metric.builder().name("Feature Adoption Rate").goal("60")
        .unit(MetricUnit.PERCENTAGE).operator(MetricOperator.GREATER_THAN).team(prod).owner(maya).build());
    allMetrics.add(Metric.builder().name("Sprint Velocity").goal("30")
        .unit(MetricUnit.NUMBER).operator(MetricOperator.GREATER_THAN).team(prod).owner(maya).build());

    allMetrics.add(Metric.builder().name("Pipeline Value").goal("2000000")
        .unit(MetricUnit.CURRENCY).operator(MetricOperator.GREATER_THAN).team(sales).owner(james).build());
    allMetrics.add(Metric.builder().name("Conversion Rate").goal("25")
        .unit(MetricUnit.PERCENTAGE).operator(MetricOperator.GREATER_THAN).team(sales).owner(james).build());

    allMetrics.add(Metric.builder().name("MQL to SQL Conversion").goal("15")
        .unit(MetricUnit.PERCENTAGE).operator(MetricOperator.GREATER_THAN).team(mktg).owner(olivia).build());
    allMetrics.add(Metric.builder().name("Website Traffic (Monthly)").goal("50000")
        .unit(MetricUnit.NUMBER).operator(MetricOperator.GREATER_THAN).team(mktg).owner(olivia).build());

    allMetrics.add(Metric.builder().name("Net Promoter Score").goal("50")
        .unit(MetricUnit.NUMBER).operator(MetricOperator.GREATER_THAN).team(cs).owner(noah).build());
    allMetrics.add(Metric.builder().name("Churn Rate").goal("3")
        .unit(MetricUnit.PERCENTAGE).operator(MetricOperator.LESS_THAN).team(cs).owner(noah).build());

    allMetrics.add(Metric.builder().name("Employee Satisfaction").goal("4.0")
        .unit(MetricUnit.NUMBER).operator(MetricOperator.GREATER_THAN).team(people).owner(ryan).build());
    allMetrics.add(Metric.builder().name("Time to Hire (Days)").goal("45")
        .unit(MetricUnit.NUMBER).operator(MetricOperator.LESS_THAN).team(people).owner(ryan).build());

    allMetrics.add(Metric.builder().name("Burn Rate").goal("350000")
        .unit(MetricUnit.CURRENCY).operator(MetricOperator.LESS_THAN).team(fin).owner(isabella).build());
    allMetrics.add(Metric.builder().name("Gross Margin").goal("75")
        .unit(MetricUnit.PERCENTAGE).operator(MetricOperator.GREATER_THAN).team(fin).owner(isabella).build());

    List<Metric> savedMetrics = metricRepository.saveAll(allMetrics);

    // Seed weekly metric values (13 weeks of data per metric, most-recent-week first)
    ensureWeeksExist();
    List<Week> recentWeeks = getRecentWeeks();
    if (recentWeeks.size() >= 13) {
      List<Week> valueWeeks = recentWeeks.subList(0, Math.min(13, recentWeeks.size()));
      // Values ordered: [current, -1, -2, ..., -12] weeks ago
      String[][] values = {
          {"14", "12", "12", "11", "11", "10", "10", "9", "9", "8", "8", "7", "7"},         // Deploy Frequency
          {"14", "14", "15", "16", "16", "17", "18", "18", "19", "20", "20", "21", "22"},     // Bug Count
          {"60", "59", "58", "56", "55", "54", "52", "50", "48", "47", "45", "44", "42"},     // Feature Adoption Rate
          {"33", "32", "31", "30", "30", "29", "28", "28", "27", "26", "25", "25", "24"},     // Sprint Velocity
          {"2150000", "2100000", "2000000", "1900000", "1850000", "1800000", "1700000",        // Pipeline Value
              "1600000", "1500000", "1400000", "1300000", "1250000", "1200000"},
          {"26", "25", "25", "24", "23", "23", "22", "21", "20", "20", "19", "18", "18"},     // Conversion Rate
          {"16", "15", "15", "14", "14", "13", "13", "12", "12", "11", "11", "10", "10"},     // MQL to SQL Conversion
          {"52000", "50000", "49000", "47500", "46000", "44000", "42500",                     // Website Traffic
              "41000", "40000", "39000", "38000", "36500", "35000"},
          {"52", "50", "49", "48", "47", "46", "45", "44", "43", "42", "41", "40", "38"},     // NPS
          {"2.8", "3.0", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "3.9", "4.0", "4.2"}, // Churn Rate
          {"4.1", "4.1", "4.0", "3.9", "3.9", "3.8", "3.8", "3.7", "3.6", "3.6", "3.5", "3.5", "3.4"}, // Employee Satisfaction
          {"42", "43", "44", "45", "46", "47", "48", "49", "50", "52", "52", "54", "55"},     // Time to Hire
          {"340000", "350000", "355000", "365000", "370000", "375000", "380000",               // Burn Rate
              "385000", "390000", "395000", "400000", "405000", "410000"},
          {"77", "76", "75", "74", "74", "73", "72", "72", "71", "70", "70", "69", "68"},     // Gross Margin
      };

      List<MetricValue> metricValues = new ArrayList<>();
      for (int m = 0; m < savedMetrics.size(); m++) {
        for (int w = 0; w < valueWeeks.size(); w++) {
          metricValues.add(MetricValue.builder()
              .metric(savedMetrics.get(m))
              .week(valueWeeks.get(w))
              .value(values[m][w])
              .build());
        }
      }
      metricValueRepository.saveAll(metricValues);
    }

    log.info("action=seed.metricsCreated count={}", savedMetrics.size());
  }

  private void ensureWeeksExist() {
    LocalDate today = LocalDate.now();
    LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    for (int i = 0; i <= 12; i++) {
      LocalDate start = thisMonday.minusWeeks(i);
      weekRepository.insertWeekIfNotExists(start, start.plusDays(6));
    }
  }

  private List<Week> getRecentWeeks() {
    List<Week> allWeeks = weekRepository.findAll();
    allWeeks.sort((a, b) -> b.getStartDate().compareTo(a.getStartDate()));
    return allWeeks.subList(0, Math.min(13, allWeeks.size()));
  }

  private void seedHeadlines(List<Team> teams, Map<String, User> userMap) {
    User sarah = userMap.get("sarah_chen");
    User marcus = userMap.get("marcus_johnson");
    User maya = userMap.get("maya_patel");
    User james = userMap.get("james_wilson");
    User olivia = userMap.get("olivia_martinez");
    User noah = userMap.get("noah_williams");
    User ryan = userMap.get("ryan_chen");

    Team eng = teams.get(0), prod = teams.get(1), sales = teams.get(2);
    Team mktg = teams.get(3), cs = teams.get(4), people = teams.get(5), fin = teams.get(6);

    Map<String, List<Headline>> headlinesByTeam = new HashMap<>();
    headlinesByTeam.put("Engineering", List.of(
        Headline.builder().title("CI pipeline time reduced to 11 minutes, on track to hit 10").team(eng).build(),
        Headline.builder().title("Billing microservice migration completed ahead of schedule").team(eng).build(),
        Headline.builder().title("Two senior engineer candidates passed technical screen").team(eng).build()));
    headlinesByTeam.put("Product", List.of(
        Headline.builder().title("Dashboard v2 user testing showing 40% faster report generation").team(prod).build(),
        Headline.builder().title("Customer discovery interviews at 15 of 20 target").team(prod).build()));
    headlinesByTeam.put("Sales", List.of(
        Headline.builder().title("Closed 2 enterprise deals this week totaling $120K ACV").team(sales).build(),
        Headline.builder().title("Outbound SDR playbook draft complete, review pending").team(sales).build(),
        Headline.builder().title("Pipeline grew by $300K this week").team(sales).build()));
    headlinesByTeam.put("Marketing", List.of(
        Headline.builder().title("Q2 case study with AcmeCorp published, strong early engagement").team(mktg).build(),
        Headline.builder().title("Newsletter subscriber count crossed 4K milestone").team(mktg).build()));
    headlinesByTeam.put("Customer Success", List.of(
        Headline.builder().title("NPS improved from 48 to 52 this quarter").team(cs).build(),
        Headline.builder().title("Onboarding academy pilot launched with 5 beta customers").team(cs).build(),
        Headline.builder().title("Churn rate dropped to 2.8%, lowest this year").team(cs).build()));
    headlinesByTeam.put("People Ops", List.of(
        Headline.builder().title("Quarterly review process approved by leadership").team(people).build(),
        Headline.builder().title("3 new hires starting next week across Engineering and Sales").team(people).build()));
    headlinesByTeam.put("Finance", List.of(
        Headline.builder().title("Cash flow dashboard prototype ready for demo").team(fin).build(),
        Headline.builder().title("Monthly burn reduced by 8% following vendor renegotiations").team(fin).build()));

    for (List<Headline> hList : headlinesByTeam.values()) {
      headlineRepository.saveAll(hList);
    }
    log.info("action=seed.headlinesCreated");
  }

  private List<Issue> seedIssues(List<Team> teams, Map<String, User> userMap) {
    Map<String, IssueType> issueTypes = new HashMap<>();
    for (IssueType it : issueTypeRepository.findAll()) {
      issueTypes.put(it.getName(), it);
    }

    IssueType problem = issueTypes.get("Problem");
    IssueType decision = issueTypes.get("Decision Needed");
    IssueType info = issueTypes.get("Info Needed");
    IssueType debrief = issueTypes.get("Debrief");
    IssueType longTerm = issueTypes.get("Long Term Issue");
    IssueType recurring = issueTypes.get("Recurring");

    Team eng = teams.get(0), prod = teams.get(1), sales = teams.get(2);
    Team mktg = teams.get(3), cs = teams.get(4), people = teams.get(5), fin = teams.get(6);

    List<Issue> allIssues = new ArrayList<>();
    allIssues.add(Issue.builder().title("CI pipeline occasionally stalls on flaky E2E tests")
        .description("End-to-end tests for the payment flow are timing out ~20% of the time, causing CI retries and delaying deployments.")
        .issueType(problem).team(eng).build());
    allIssues.add(Issue.builder().title("Should we adopt Kubernetes for the new microservices?")
        .description("With Phase 2 of the microservice migration approaching, we need to decide whether to use Kubernetes or stick with EC2.")
        .issueType(decision).team(eng).build());
    allIssues.add(Issue.builder().title("Document API contracts before microservice launch")
        .description("The mobile team needs finalized API contracts for the notification service at least 2 weeks before launch.")
        .issueType(info).team(eng).build());
    allIssues.add(Issue.builder().title("Technical debt in legacy billing module")
        .description("The legacy billing module has accumulated significant technical debt that needs to be addressed before full deprecation.")
        .issueType(longTerm).team(eng).build());

    allIssues.add(Issue.builder().title("Dashboard v2 search latency too high with 10K+ records")
        .description("The search functionality in the dashboard v2 prototype degrades significantly with large datasets. Needs optimization.")
        .issueType(problem).team(prod).build());
    allIssues.add(Issue.builder().title("Pricing tier naming finalization needed")
        .description("Marketing needs the final pricing tier names (Starter, Growth, Enterprise vs. Basic, Pro, Enterprise) to update the website.")
        .issueType(decision).team(prod).build());

    allIssues.add(Issue.builder().title("Enterprise deals stalling at security review stage")
        .description("Multiple enterprise prospects are getting stuck at the security review stage. We need to streamline our security response documentation.")
        .issueType(problem).team(sales).build());
    allIssues.add(Issue.builder().title("CRM integration with HubSpot needs cleanup")
        .description("Duplicate contacts and outdated pipeline stages in HubSpot are making reporting unreliable.")
        .issueType(recurring).team(sales).build());

    allIssues.add(Issue.builder().title("Blog SEO traffic dropped 15% after site migration")
        .description("The recent website migration caused several blog posts to lose SEO rankings. Need to audit and fix redirects.")
        .issueType(problem).team(mktg).build());
    allIssues.add(Issue.builder().title("Case study interview scheduling process")
        .description("Need to establish a smoother process for scheduling customer case study interviews to avoid delays.")
        .issueType(debrief).team(mktg).build());

    allIssues.add(Issue.builder().title("Top 3 at-risk accounts need intervention plans")
        .description("Three enterprise accounts have shown declining usage patterns. Need to develop account-specific intervention plans.")
        .issueType(problem).team(cs).build());
    allIssues.add(Issue.builder().title("Onboarding academy content review cycle")
        .description("Who reviews and approves the onboarding academy content before publishing? Need to define the workflow.")
        .issueType(decision).team(cs).build());

    allIssues.add(Issue.builder().title("Benefits renewal timeline for Q3")
        .description("Benefits renewal deadline is approaching. Need to decide on carrier changes and communicate to the team.")
        .issueType(decision).team(people).build());

    allIssues.add(Issue.builder().title("Invoice discrepancies with AWS billing")
        .description("There are recurring discrepancies between our internal usage tracking and AWS invoices. Need to investigate and reconcile.")
        .issueType(problem).team(fin).build());

    List<Issue> saved = issueRepository.saveAll(allIssues);
    log.info("action=seed.issuesCreated count={}", saved.size());
    return saved;
  }

  private void seedTodos(List<Team> teams, Map<String, User> userMap, List<Issue> issues) {
    User marcus = userMap.get("marcus_johnson");
    User elena = userMap.get("elena_rodriguez");
    User david = userMap.get("david_kim");
    User maya = userMap.get("maya_patel");
    User alex = userMap.get("alex_thompson");
    User james = userMap.get("james_wilson");
    User sophia = userMap.get("sophia_lee");
    User olivia = userMap.get("olivia_martinez");
    User liam = userMap.get("liam_o'brien");
    User noah = userMap.get("noah_williams");
    User emma = userMap.get("emma_davis");
    User ryan = userMap.get("ryan_chen");

    // Map issues by title prefix for linking
    Issue flakyTestIssue = findIssueByTitle(issues, "CI pipeline occasionally stalls");
    Issue kubernetesIssue = findIssueByTitle(issues, "Should we adopt Kubernetes");
    Issue apiContractIssue = findIssueByTitle(issues, "Document API contracts");
    Issue securityIssue = findIssueByTitle(issues, "Enterprise deals stalling");
    Issue seoIssue = findIssueByTitle(issues, "Blog SEO traffic dropped");
    Issue atRiskIssue = findIssueByTitle(issues, "Top 3 at-risk accounts");

    LocalDate tomorrow = LocalDate.now().plusDays(1);
    LocalDate nextWeek = LocalDate.now().plusDays(7);
    LocalDate twoWeeks = LocalDate.now().plusDays(14);

    List<Todo> allTodos = new ArrayList<>();

    // Engineering
    allTodos.add(Todo.builder().title("Fix flaky E2E test for payment flow")
        .description("Investigate and fix the timeout issue in payment flow E2E tests.").status(TodoStatus.IN_PROGRESS)
        .dueDate(tomorrow.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(0)).assignees(new HashSet<>(Set.of(elena))).issue(flakyTestIssue).build());
    allTodos.add(Todo.builder().title("Research Kubernetes vs EC2 cost comparison")
        .description("Put together a cost analysis of K8s vs EC2 for the new microservices.").status(TodoStatus.NOT_STARTED)
        .dueDate(nextWeek.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(0)).assignees(new HashSet<>(Set.of(david))).issue(kubernetesIssue).build());
    allTodos.add(Todo.builder().title("Draft API contracts for notification service")
        .description("Write the initial API contracts for the notification microservice.").status(TodoStatus.NOT_STARTED)
        .dueDate(twoWeeks.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(0)).assignees(new HashSet<>(Set.of(david, elena))).issue(apiContractIssue).build());
    allTodos.add(Todo.builder().title("Deploy hotfix for login redirect bug")
        .description("Users are getting a blank page after login on Safari. Deploy the fix to production.").status(TodoStatus.COMPLETED)
        .team(teams.get(0)).assignees(new HashSet<>(Set.of(elena))).build());

    // Product
    allTodos.add(Todo.builder().title("Optimize dashboard search query")
        .description("Work with Engineering to optimize the search query for dashboard v2.").status(TodoStatus.IN_PROGRESS)
        .dueDate(nextWeek.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(1)).assignees(new HashSet<>(Set.of(alex, david))).build());
    allTodos.add(Todo.builder().title("Finalize pricing tier names with Marketing")
        .description("Sit down with Marketing to finalize pricing tier names this week.").status(TodoStatus.NOT_STARTED)
        .dueDate(tomorrow.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(1)).assignees(new HashSet<>(Set.of(maya, olivia))).build());
    allTodos.add(Todo.builder().title("Conduct remaining 5 customer interviews")
        .description("Schedule and conduct the remaining 5 customer discovery interviews.").status(TodoStatus.IN_PROGRESS)
        .team(teams.get(1)).assignees(new HashSet<>(Set.of(alex))).build());

    // Sales
    allTodos.add(Todo.builder().title("Create security response document template")
        .description("Build a reusable security response document to speed up enterprise deal security reviews.").status(TodoStatus.IN_PROGRESS)
        .dueDate(nextWeek.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(2)).assignees(new HashSet<>(Set.of(james))).issue(securityIssue).build());
    allTodos.add(Todo.builder().title("Clean up duplicate contacts in HubSpot")
        .description("Merge 50+ duplicate contact records in HubSpot CRM.").status(TodoStatus.NOT_STARTED)
        .team(teams.get(2)).assignees(new HashSet<>(Set.of(sophia))).build());
    allTodos.add(Todo.builder().title("Follow up with AcmeCorp on Q2 renewal")
        .description("AcmeCorp renewal is due next month. Schedule a QBR and send renewal proposal.").status(TodoStatus.COMPLETED)
        .team(teams.get(2)).assignees(new HashSet<>(Set.of(sophia))).build());

    // Marketing
    allTodos.add(Todo.builder().title("Fix SEO redirects from site migration")
        .description("Audit and fix broken redirects that are causing SEO ranking drops.").status(TodoStatus.IN_PROGRESS)
        .dueDate(nextWeek.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(3)).assignees(new HashSet<>(Set.of(olivia, liam))).issue(seoIssue).build());
    allTodos.add(Todo.builder().title("Write Q2 case study with AcmeCorp")
        .description("Draft the full case study based on the AcmeCorp interview.").status(TodoStatus.IN_PROGRESS)
        .dueDate(tomorrow.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(3)).assignees(new HashSet<>(Set.of(liam))).build());
    allTodos.add(Todo.builder().title("Design newsletter lead magnet PDF")
        .description("Design a lead magnet PDF to boost newsletter signups.").status(TodoStatus.NOT_STARTED)
        .team(teams.get(3)).assignees(new HashSet<>(Set.of(olivia))).build());

    // Customer Success
    allTodos.add(Todo.builder().title("Draft intervention plans for top 3 at-risk accounts")
        .description("Create account-specific intervention plans with timelines and success criteria.").status(TodoStatus.IN_PROGRESS)
        .dueDate(nextWeek.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(4)).assignees(new HashSet<>(Set.of(noah, emma))).issue(atRiskIssue).build());
    allTodos.add(Todo.builder().title("Record onboarding academy intro video")
        .description("Record the introductory walkthrough video for the new onboarding academy.").status(TodoStatus.NOT_STARTED)
        .dueDate(twoWeeks.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(4)).assignees(new HashSet<>(Set.of(emma))).build());
    allTodos.add(Todo.builder().title("Send NPS survey to Q2 onboarded customers")
        .description("Trigger NPS survey to all customers who onboarded in Q2.").status(TodoStatus.COMPLETED)
        .team(teams.get(4)).assignees(new HashSet<>(Set.of(noah))).build());

    // People Ops
    allTodos.add(Todo.builder().title("Prepare offer letters for 3 new hires")
        .description("Prepare and send offer letters for the 3 candidates who accepted.").status(TodoStatus.IN_PROGRESS)
        .team(teams.get(5)).assignees(new HashSet<>(Set.of(ryan))).build());
    allTodos.add(Todo.builder().title("Schedule Q3 benefits renewal meeting")
        .description("Schedule a meeting with the benefits broker to review Q3 renewal options.").status(TodoStatus.NOT_STARTED)
        .team(teams.get(5)).assignees(new HashSet<>(Set.of(ryan))).build());

    // Finance
    allTodos.add(Todo.builder().title("Reconcile AWS invoice discrepancies")
        .description("Work with Engineering to reconcile the AWS billing discrepancies for the past 2 months.").status(TodoStatus.IN_PROGRESS)
        .team(teams.get(6)).assignees(new HashSet<>(Set.of(marcus))).build());
    allTodos.add(Todo.builder().title("Prepare monthly financial report for board")
        .description("Compile the monthly financial report including P&L, cash flow, and burn rate analysis.").status(TodoStatus.NOT_STARTED)
        .dueDate(tomorrow.atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        .team(teams.get(6)).assignees(new HashSet<>()).build());

    todoRepository.saveAll(allTodos);
    log.info("action=seed.todosCreated count={}", allTodos.size());
  }

  private Issue findIssueByTitle(List<Issue> issues, String prefix) {
    return issues.stream().filter(i -> i.getTitle().startsWith(prefix)).findFirst().orElse(null);
  }

  private void seedMeetings(List<Team> teams, Team leadershipTeam) {
    LocalTime meetingTime = LocalTime.of(9, 0);
    LocalDate today = LocalDate.now();
    LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    // All teams including Leadership
    List<Team> allTeams = new ArrayList<>(teams);
    allTeams.add(leadershipTeam);

    for (Team team : allTeams) {
      List<User> teamUsers = userRepository.findAll().stream()
          .filter(u -> u.getTeams().stream().anyMatch(t -> t.getId().equals(team.getId())))
          .toList();

      if (teamUsers.isEmpty()) continue;

      User facilitator = teamUsers.get(0);
      User scribe = teamUsers.size() > 1 ? teamUsers.get(1) : teamUsers.get(0);
      User scheduler = teamUsers.size() > 2 ? teamUsers.get(2) : teamUsers.get(0);

      // 2 past weeks - create finished meetings directly
      for (int i = 2; i >= 1; i--) {
        LocalDate pastMonday = thisMonday.minusWeeks(i);
        LocalDate pastMeetingDate = pastMonday;

        L10Meeting pastMeeting = L10Meeting.builder()
            .team(team)
            .meetingDate(pastMeetingDate)
            .meetingTime(meetingTime)
            .weekStartDate(pastMonday)
            .facilitator(facilitator)
            .scribe(scribe)
            .status(L10MeetingStatus.FINISHED)
            .concludeKeyDecisions("Reviewed " + (i == 2 ? "last" : "this") + " week's metrics and assigned owners for top 3 issues.")
            .concludeCascadingMessage("Key updates shared with " + team.getName() + " team.")
            .build();

        l10MeetingRepository.save(pastMeeting);
      }

      // 3 future weeks - schedule via service
      for (int i = 1; i <= 3; i++) {
        LocalDate nextMonday = thisMonday.plusWeeks(i);
        CreateL10MeetingRequest request = new CreateL10MeetingRequest(
            team.getId(), nextMonday, meetingTime, facilitator.getId(), scribe.getId());

        try {
          l10MeetingService.scheduleMeeting(request, scheduler.getId());
        } catch (Exception e) {
          log.warn("action=seed.meetingSkipped teamId={} date={} error={}",
              team.getId(), nextMonday, e.getMessage());
        }
      }
    }

    log.info("action=seed.meetingsCreated");
  }
}
