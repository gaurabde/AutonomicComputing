package usr.code;



import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import java.io.*;
import java.util.*;

public class WordCountBetweenFiles extends Configured implements Tool {

	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, IntWritable> {
		
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		//private boolean caseSensitive = true;
		private Set<String> patternsToFind = new HashSet<String>();
		static enum Counters {
			INPUT_WORDS
		}
	
		//private long numRecords = 0;
		private String inputFile;

		public void configure(JobConf job) {
			inputFile = job.get("map.input.file");

			Path[] distPatternFiles = new Path[0];
			try {
				distPatternsFiles = DistributedCache.getLocalCacheFiles(job);
			} catch (IOException ioe) {
				System.err.println("Exception accessing cache data: " + StringUtils.stringifyException(ioe));
			}
			for (Path patternsFile : distPatternsFiles) {
				LoadHashFile(distPatternsFiles);
			}
			
		}

		

		public void map(LongWritable key, Text value,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			String line = value.toString();

			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				word.set(tokenizer.nextToken());
				if(patternsToFind.contains(word.toString()))
					output.collect(word, one);
			}
		}

    		private void LoadHashFile(Path bufferFile) {
			try {
				BufferedReader fis = new BufferedReader(new FileReader(bufferFile.toString()));
				String word = null;
				while ((pattern = fis.readLine()) != null) 
				{
					StringTokenizer tokenizer = new StringTokenizer(word);
					while (tokenizer.hasMoreTokens()) 
					{
						patternsToFind.add(tokenizer.nextToken());
					}
				}
			} catch (IOException e) {
				System.err.println("Exception Parsing cached data file name: '"+ patternsFile+ "' : "+ StringUtils.stringifyException(e));
			}
		}
	}

	public static class Reducer extends MapReduceBase implements
			Reducer<Text, IntWritable, Text, IntWritable> {
		public void reduce(Text key, Iterator<IntWritable> values,OutputCollector<Text, IntWritable> context, Reporter reporter)
				throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			context.collect(key, new IntWritable(sum));
		}
	}

	public int run(String[] args) throws Exception {
		JobConf conf = new JobConf(getConf(), WordCountBetweenFiles.class);
		conf.setJobName("wordcount");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reducer.class);
		conf.setReducerClass(Reducer.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		DistributedCache.addCacheFile(new Path(args[2]).toUri(), conf);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int outputCode = ToolRunner.run(new Configuration(), new WordCountBetweenFiles(), args);
		System.exit(outputCode);
	}
}
