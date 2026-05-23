package com.ces.eos.seed;

import com.ces.eos.dto.request.CreateL10MeetingRequest;
import com.ces.eos.entity.Role;
import com.ces.eos.entity.Team;
import com.ces.eos.entity.User;
import com.ces.eos.enums.UserRole;
import com.ces.eos.repository.L10MeetingRepository;
import com.ces.eos.repository.RoleRepository;
import com.ces.eos.repository.TeamRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.service.L10MeetingService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@Order(100)
@RequiredArgsConstructor
public class L10MeetingSeeder implements CommandLineRunner {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final L10MeetingRepository l10MeetingRepository;
  private final L10MeetingService l10MeetingService;

  @Override
  public void run(String... args) {
    if (teamRepository.count() > 1) {
      log.info("action=seed.skip reason=dataAlreadyExists");
      return;
    }

    Role userRole =
        roleRepository
            .findByName(UserRole.USER)
            .orElseThrow(() -> new IllegalStateException("USER role not found"));

    Role adminRole =
        roleRepository
            .findByName(UserRole.ADMIN)
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

    Team engineering =
        teamRepository.save(
            Team.builder().name("Engineering").isLeadership(false).timezone("America/New_York").build());

    Team marketing =
        teamRepository.save(
            Team.builder().name("Marketing").isLeadership(false).timezone("America/New_York").build());

    Team leadership =
        teamRepository.save(
            Team.builder().name("Leadership").isLeadership(true).timezone("America/New_York").build());

    log.info(
        "action=seed.teamsCreated engineeringId={} marketingId={} leadershipId={}",
        engineering.getId(),
        marketing.getId(),
        leadership.getId());

    User alice =
        userRepository.save(
            User.builder()
                .firstName("Lezt")
                .lastName("Waper")
                .email("leztwaper@gmail.com")
                .role(adminRole)
                .isActive(true)
                .teams(new HashSet<>(Set.of(engineering, leadership)))
                .build());

    User bob =
        userRepository.save(
            User.builder()
                .firstName("Bob")
                .lastName("Smith")
                .email("bob@example.com")
                .role(userRole)
                .isActive(true)
                .teams(new HashSet<>(Set.of(engineering)))
                .build());

    User carol =
        userRepository.save(
            User.builder()
                .firstName("Carol")
                .lastName("Williams")
                .email("carol@example.com")
                .role(userRole)
                .isActive(true)
                .teams(new HashSet<>(Set.of(engineering)))
                .build());

    User dave =
        userRepository.save(
            User.builder()
                .firstName("Dave")
                .lastName("Brown")
                .email("dave@example.com")
                .role(userRole)
                .isActive(true)
                .teams(new HashSet<>(Set.of(marketing)))
                .build());

    User eve =
        userRepository.save(
            User.builder()
                .firstName("Eve")
                .lastName("Davis")
                .email("eve@example.com")
                .role(userRole)
                .isActive(true)
                .teams(new HashSet<>(Set.of(marketing)))
                .build());

    log.info(
        "action=seed.usersCreated aliceId={} bobId={} carolId={} daveId={} eveId={}",
        alice.getId(),
        bob.getId(),
        carol.getId(),
        dave.getId(),
        eve.getId());

    scheduleL10Meetings(engineering, alice, bob, carol);
    scheduleL10Meetings(marketing, dave, eve, alice);

    log.info("action=seed.complete");
  }

  private void scheduleL10Meetings(Team team, User facilitator, User scribe, User scheduler) {
    LocalDate today = LocalDate.now();
    LocalTime meetingTime = LocalTime.of(9, 0);

    for (int i = 0; i < 3; i++) {
      LocalDate meetingDate = today.plusWeeks(i + 1).with(java.time.DayOfWeek.MONDAY);

      CreateL10MeetingRequest request =
          new CreateL10MeetingRequest(
              team.getId(), meetingDate, meetingTime, facilitator.getId(), scribe.getId());

      l10MeetingService.scheduleMeeting(request, scheduler.getId());
      log.info(
          "action=seed.meetingScheduled teamId={} date={} time={}",
          team.getId(), meetingDate, meetingTime);
    }
  }
}
