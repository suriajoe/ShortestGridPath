public class PerformanceTest {
        private static final long MEGABYTE = 1024L * 1024L;

        public static long bytesToMegabytes(long bytes) {
                return bytes / MEGABYTE;
        }

        public void memUsageBefore() {
                // Get the Java runtime
                Runtime runtime = Runtime.getRuntime();
                // Run the garbage collector
                runtime.gc();
                // Calculate the used memory
                long memory = runtime.totalMemory() - runtime.freeMemory();
                System.out.println("Before path Algorithm, Used memory is bytes: " + memory);
               // System.out.println("Before path Algorithm, Used memory is megabytes: "
               //                 + bytesToMegabytes(memory));
        }
        public void memUsageAfter() {
            // Get the Java runtime
            Runtime runtime = Runtime.getRuntime();
            // Run the garbage collector
            runtime.gc();
            // Calculate the used memory
            long memory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("After path Algorithm, Used memory is bytes: " + memory);
            //System.out.println("After path, Used memory is megabytes: "
            //                + bytesToMegabytes(memory));
        }
        
        public long memUsage()
        {
        	Runtime runtime = Runtime.getRuntime();
        	runtime.gc();
        	long memory = runtime.totalMemory() - runtime.freeMemory();
        	return memory;
        }
}