package frc.robot.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Autonomous policy lives here so route fallbacks and requirements can be
 * changed without editing AutoManager.
 */
public final class AutoConstants {
  public static final String DEFAULT_SAFE_FALLBACK = "Do Nothing";
  public static final double MATCH_DURATION_SECONDS = 15.0;
  public static final double DEFAULT_AUTO_TIMEOUT_SECONDS = 14.5;
  public static final double SCORING_ACTION_ESTIMATED_SECONDS = 1.0;
  public static final double SCORING_TIME_SAFETY_MARGIN_SECONDS = 1.0;
  public static final boolean ENABLE_EXAMPLE_ROUTINE = false;
  public static final Map<String, List<String>> FALLBACKS = Map.of(
      "Example: Time Aware", List.of("Safe Defense", DEFAULT_SAFE_FALLBACK));
  public static final Set<String> REQUIRES_TRUSTED_VISION = Set.of();
  public static final Set<String> REQUIRES_READY_SUPERSTRUCTURE = Set.of();
  public static final Set<String> ABORTS_ON_VISION_LOSS = Set.of();
  public static final Set<String> ABORTS_ON_SUPERSTRUCTURE_FAULT = Set.of("Example: Time Aware");
  public static final Map<String, Double> TIMEOUT_SECONDS = Map.of();

  private AutoConstants() {}

  public static List<String> fallbacksFor(String autoName) {
    if (FALLBACKS.containsKey(autoName)) {
      return FALLBACKS.get(autoName);
    }
    return DEFAULT_SAFE_FALLBACK.equals(autoName)
        ? List.of()
        : List.of(DEFAULT_SAFE_FALLBACK);
  }

  public static double timeoutFor(String autoName) {
    return DEFAULT_SAFE_FALLBACK.equals(autoName)
        ? 0.0
        : TIMEOUT_SECONDS.getOrDefault(autoName, DEFAULT_AUTO_TIMEOUT_SECONDS);
  }
}
