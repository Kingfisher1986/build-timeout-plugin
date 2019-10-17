package hudson.plugins.build_timeout.impl;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.plugins.build_timeout.BuildTimeOutOperation;
import hudson.plugins.build_timeout.BuildTimeOutStrategy;
import hudson.plugins.build_timeout.BuildTimeOutStrategyDescriptor;
import hudson.plugins.build_timeout.BuildTimeoutWrapper;
import hudson.plugins.build_timeout.operations.AbortOperation;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.xml.datatype.Duration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * If the build took longer than <tt>timeoutDate</tt> amount of milliseconds, it will be terminated.
 */
public class DateTimeOutStrategy extends BuildTimeOutStrategy {
    private final String timeoutDate;

    public String getTimeoutDate() {
        return timeoutDate;
    }

    @DataBoundConstructor
    public DateTimeOutStrategy(String timeoutDate) {
        this.timeoutDate = timeoutDate;
    }

    @Override
    public long getTimeOut(@Nonnull AbstractBuild<?, ?> build, @Nonnull BuildListener listener) {
        // * * * * * Minute Hour DayOfMonth Month Weekday
        List<String> items = Arrays.asList(getTimeoutDate().split(" "));
        int minute = 0;
        int hour = 0;
        int dayOfMonth = 0;
        int month = 0;
        int weekday = 0;

        Calendar calendar = Calendar.getInstance();

        if (items.get(0).chars().allMatch(Character::isDigit)) {
            minute = Integer.parseInt(items.get(0));
        } else if ("*".equals(items.get(0))) {
            minute = calendar.get(Calendar.MINUTE);
        }
        if (items.get(1).chars().allMatch(Character::isDigit)) {
            hour = Integer.parseInt(items.get(1));
        } else if ("*".equals(items.get(1))) {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        }
        if (items.get(2).chars().allMatch(Character::isDigit)) {
            dayOfMonth = Integer.parseInt(items.get(2));
        } else if ("*".equals(items.get(2))) {
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }
        if (items.get(3).chars().allMatch(Character::isDigit)) {
            month = Integer.parseInt(items.get(3)) - 1; // -1 because Java Calendar starts months with January = 0
        } else if ("*".equals(items.get(3))) {
            month = calendar.get(Calendar.MONTH);
        }
        if (items.get(4).chars().allMatch(Character::isDigit)) {
            weekday = Integer.parseInt(items.get(4)) + 1; // + 1 because Java Calendar...
        } else if ("*".equals(items.get(4))) {
            weekday = calendar.get(Calendar.DAY_OF_WEEK);
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_WEEK, weekday);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return (calendar.getTimeInMillis() > System.currentTimeMillis()) ? calendar.getTimeInMillis() - System.currentTimeMillis() : System.currentTimeMillis() - calendar.getTimeInMillis();
    }

    @Override
    public Descriptor<BuildTimeOutStrategy> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension(ordinal = 100) // This is displayed at the top as the default
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildTimeOutStrategyDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.DateTimeOutStrategy_DisplayName();
        }

        @Override
        public boolean isApplicableAsBuildStep() {
            return true;
        }
    }
}
