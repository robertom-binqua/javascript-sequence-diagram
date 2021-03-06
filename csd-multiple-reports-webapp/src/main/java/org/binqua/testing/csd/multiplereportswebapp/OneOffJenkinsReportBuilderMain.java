package org.binqua.testing.csd.multiplereportswebapp;

import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;

import org.binqua.testing.csd.common.ManifestReader;

public class OneOffJenkinsReportBuilderMain {

    public static void main(String[] args) {
        final DateTimeFormatter dateTimeFormatter = new JodaTimeFormatterImpl();
        final SupportNotifierFactory supportNotifierFactory = OneOffJenkinsReportBuilderMain::createSupportNotifier;
        final ReportBuilderConfiguration configuration = new OptionsReportBuilderConfiguration(new ManifestReader(), args);

        new ReportRunnable(
            new FilteredByNumberOfBuildsReportDirectoryScannerDecorator(
                new ReportDirectoryScannerImpl(
                    supportNotifierFactory,
                    configuration.reportDirectoryRegexMatcher(),
                    configuration.directoryToScan())
            ),
            new JenkinsBuildReportResource(),
            new MustacheBuildsSummaryGenerator(new PrettyFormatSummaryFormatter(new JsonBuildsSummaryFormatter(configuration)), configuration, dateTimeFormatter, DateTime::new),
            new AssetsWriter(configuration.reportDirectoryRoot()),
            supportNotifierFactory,
            configuration.scanPeriodInSecs(),
            configuration.csdBuildNumber()
        ).run();
    }

    private static SupportNotifier createSupportNotifier(final Class<?> clazz) {
        return new SupportNotifier() {
            @Override
            public void info(String message) {
                LoggerFactory.getLogger(clazz).info(message);
            }

            @Override
            public void info(String message, Exception e) {
                LoggerFactory.getLogger(clazz).info(message, e);
            }
        };
    }

}
