package org.forestdept.seemsai.config

/**
 * Central configuration for Forest Department emergency helpline and operational defaults.
 * Configured in one place for safety and field adaptability.
 */
object ForestConfig {
    /**
     * Standard 24x7 Wildlife Emergency & Forest Helpline (Ministry of Environment, Forest & Climate Change).
     * When configured, the app launches the system dialer with this prefilled number.
     */
    const val FOREST_DEPT_HELPLINE: String = "1926"

    /**
     * Display label for the emergency helpline.
     */
    const val FOREST_DEPT_HELPLINE_LABEL: String = "Forest Dept 24x7 Wildlife Helpline (1926)"

    /**
     * Optional secondary corridor control room number.
     */
    const val DALMA_CORRIDOR_CONTROL_ROOM: String = "18004254733"
}
