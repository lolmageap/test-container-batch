package cherhy.batch.settlement.file

import cherhy.batch.settlement.jdbc.example.process.ExampleItemProcessor.Companion.DISCOUNT_PRICE
import cherhy.batch.settlement.model.Example
import cherhy.batch.settlement.util.property.BatchProperties.CHUNK_SIZE
import cherhy.batch.settlement.util.property.BatchProperties.ItemProcessor.CSV_FILE_ITEM_PROCESSOR
import cherhy.batch.settlement.util.property.BatchProperties.ItemProcessor.TEXT_FILE_ITEM_PROCESSOR
import cherhy.batch.settlement.util.property.BatchProperties.ItemReader.CSV_FILE_ITEM_READER
import cherhy.batch.settlement.util.property.BatchProperties.ItemReader.TEXT_FILE_ITEM_READER
import cherhy.batch.settlement.util.property.BatchProperties.ItemWriter.CSV_FILE_ITEM_WRITER
import cherhy.batch.settlement.util.property.BatchProperties.ItemWriter.TEXT_FILE_ITEM_WRITER
import cherhy.batch.settlement.util.property.BatchProperties.Job.EXAMPLE_FILE_JOB
import cherhy.batch.settlement.util.property.BatchProperties.Step.EXAMPLE_FILE_LAST_STEP
import cherhy.batch.settlement.util.property.BatchProperties.Step.EXAMPLE_FILE_FIRST_STEP
import cherhy.batch.settlement.util.property.FilePath.INPUT_CSV_FILE_NAME
import cherhy.batch.settlement.util.property.FilePath.INPUT_TEXT_FILE_NAME
import cherhy.batch.settlement.util.property.FilePath.OUTPUT_CSV_FILE_NAME
import cherhy.batch.settlement.util.property.FilePath.OUTPUT_TEXT_FILE_NAME
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor
import org.springframework.batch.item.file.transform.DelimitedLineAggregator
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class FileBatchJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val inputCsvFileResource = FileSystemResource(INPUT_CSV_FILE_NAME)
    private val outputCsvFileResource = FileSystemResource(OUTPUT_CSV_FILE_NAME)
    private val inputTextFileResource = FileSystemResource(INPUT_TEXT_FILE_NAME)
    private val outputTextFileResource = FileSystemResource(OUTPUT_TEXT_FILE_NAME)

    @Bean
    fun fileJob() =
        JobBuilder(EXAMPLE_FILE_JOB, jobRepository)
            .start(fileFirstStep())
            .next(fileLastStep())
            .build()

    @Bean(EXAMPLE_FILE_FIRST_STEP)
    fun fileFirstStep() = StepBuilder(
        EXAMPLE_FILE_FIRST_STEP,
        jobRepository,
    ).chunk<Example, Example>(CHUNK_SIZE, transactionManager)
        .reader(csvFileItemReader())
        .processor(csvFileItemProcessor())
        .writer(csvFileItemWriter())
        .build()

    @Bean(EXAMPLE_FILE_LAST_STEP)
    fun fileLastStep() = StepBuilder(
        EXAMPLE_FILE_LAST_STEP,
        jobRepository,
    ).chunk<Example, Example>(CHUNK_SIZE, transactionManager)
        .reader(csvFileItemReader())
        .processor(csvFileItemProcessor())
        .writer(csvFileItemWriter())
        .build()

    @Bean(CSV_FILE_ITEM_READER)
    fun csvFileItemReader(): ItemReader<Example> {
        val lineMapper =
            DefaultLineMapper<Example>().apply {
                setLineTokenizer(DelimitedLineTokenizer())
                setFieldSetMapper(ExampleFieldSetMapper())
            }

        return FlatFileItemReader<Example>().apply {
            setResource(inputCsvFileResource)
            setLineMapper(lineMapper)
        }
    }

    @Bean(CSV_FILE_ITEM_PROCESSOR)
    fun csvFileItemProcessor() =
        ItemProcessor<Example, Example> {
            val discountedPrice = it.price - DISCOUNT_PRICE
            it.copy(price = discountedPrice)
        }

    @Bean(CSV_FILE_ITEM_WRITER)
    fun csvFileItemWriter(): ItemWriter<Example> {
        val beanWrapperFieldExtractor =
            BeanWrapperFieldExtractor<Example>().apply {
                setNames(Example.memberPropertyNames)
            }

        val lineAggregator =
            DelimitedLineAggregator<Example>().apply {
                setFieldExtractor(beanWrapperFieldExtractor)
            }

        return FlatFileItemWriter<Example>().apply {
            setResource(outputCsvFileResource)
            setLineAggregator(lineAggregator)
        }
    }

    @Bean(TEXT_FILE_ITEM_READER)
    fun textFileItemReader(): ItemReader<Example> {
        TODO("Not yet implemented")
    }

    @Bean(TEXT_FILE_ITEM_PROCESSOR)
    fun textFileItemProcessor(): ItemProcessor<Example, Example> {
        TODO("Not yet implemented")
    }

    @Bean(TEXT_FILE_ITEM_WRITER)
    fun textFileItemWriter(): ItemWriter<Example> {
        TODO("Not yet implemented")
    }
}