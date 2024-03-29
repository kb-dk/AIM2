package dk.kb.aim.google;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import dk.kb.aim.Configuration;
import dk.kb.aim.TestUtils;
import dk.kb.aim.google.GoogleRetreiver;
import dk.kb.aim.model.Image;
import dk.kb.aim.model.Word;
import dk.kb.aim.model.WordConfidence;
import dk.kb.aim.model.WordCount;
import dk.kb.aim.repository.ImageRepository;
import dk.kb.aim.repository.ImageStatus;
import dk.kb.aim.repository.WordRepository;
import dk.kb.aim.utils.ImageConverter;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by dgj on 26-03-2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoogleRetreiverTest {

    @Autowired
    WordRepository wordRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    Configuration conf;

    @Before
    public void setup() throws Exception {
        Assert.assertNotNull(conf);
        for(Image image : imageRepository.listAllImages()) {
            imageRepository.removeImage(image);
        }
        Assert.assertTrue(imageRepository.listAllImages().isEmpty());

        // Requires the environment variable: GOOGLE_APPLICATION_CREDENTIALS
        File googleFile = new File("AIMapis.json");
        Assert.assertTrue(googleFile.isFile());
        TestUtils.injectEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", googleFile.getAbsolutePath());
    }

//    @Test
    public void testInjectEnvironmentVariable() throws Exception {
        Assert.assertNull(System.getenv("FOOBAR_ENV"));

        TestUtils.injectEnvironmentVariable("FOOBAR_ENV", "Foo");

        Assert.assertEquals(System.getenv("FOOBAR_ENV"), "Foo");
    }

    @Test
    public void testRetrieveColor() throws Exception {
        GoogleRetreiver googleRetreiver = new GoogleRetreiver();
        googleRetreiver.wordRepository = wordRepository;
        googleRetreiver.imageRepository = imageRepository;
        String tiffPath = "src" + File.separator +
                "test" + File.separator +
                "resources" + File.separator +
                "KE051541.tif";

        GoogleImage gImage = convertImage(new File(tiffPath));

        Image dbImage = mock(Image.class);

        googleRetreiver.retrieveColor(dbImage, gImage);

        verify(dbImage).setColor(Mockito.anyString());
        verify(dbImage).getPath();
        verify(dbImage).getCumulusId();
        verify(dbImage).getCategory();
        verify(dbImage).getStatus();
        verify(dbImage).getOcr();
        verify(dbImage).getIsFront();
        verify(dbImage).getId();
        verifyNoMoreInteractions(dbImage);
    }

    @Test
    public void testRetrieveLabels() throws Exception {
        GoogleRetreiver googleRetreiver = new GoogleRetreiver();
        googleRetreiver.wordRepository = wordRepository;
        googleRetreiver.imageRepository = imageRepository;
        googleRetreiver.conf = conf;
        String tiffPath = "src" + File.separator +
                "test" + File.separator +
                "resources" + File.separator +
                "KE051541.tif";

        String cumulusId = UUID.randomUUID().toString();

        GoogleImage gImage = convertImage(new File(tiffPath));

        Image dbImage = new Image(-1,"/tmp/test.jpg", cumulusId,"category","red","ocr", ImageStatus.NEW, true);
        int id = imageRepository.createImage(dbImage);
        dbImage.setId(id);

        Assert.assertEquals(wordRepository.getImageWords(id), Lists.emptyList());

        googleRetreiver.retrieveLabels(dbImage, gImage);

        List<WordConfidence> words = wordRepository.getImageWords(id);
        Assert.assertFalse(words.isEmpty());
        Assert.assertEquals(3, words.size());
    }

    @Test
    public void testTranslateText() throws IOException {
        GoogleRetreiver googleRetreiver = new GoogleRetreiver();
        googleRetreiver.wordRepository = wordRepository;
        googleRetreiver.imageRepository = imageRepository;

        String textToTranslate = "This is a test";
        String translatedText = googleRetreiver.translateText(textToTranslate);

        Assert.assertEquals("Dette er en test", translatedText);
    }

    @Test
    public void testTextExtraction() throws Exception {
        GoogleRetreiver googleRetreiver = new GoogleRetreiver();
        googleRetreiver.wordRepository = wordRepository;
        googleRetreiver.imageRepository = imageRepository;
        String tiffPath = "src" + File.separator +
                         "test" + File.separator +
                         "resources" + File.separator +
                         "KE051541.tif";

        GoogleImage image = convertImage(new File(tiffPath));

        List<AnnotateImageResponse> responses = googleRetreiver.sendRequest(image, Feature.Type.TEXT_DETECTION);

        for(AnnotateImageResponse response : responses) {
            System.out.println("TEXT_DETECTION:");
            System.out.println(response.getFullTextAnnotation().getText());
            System.out.println("--------");
        }
        responses = googleRetreiver.sendRequest(image, Feature.Type.DOCUMENT_TEXT_DETECTION);

        for(AnnotateImageResponse response : responses) {
            System.out.println("DOCUMENT_TEXT_DETECTION:");
            System.out.println(response.getFullTextAnnotation().getText());
            System.out.println("--------");
        }
    }
    
//    @Test
    // DOES NOT WORK, SINCE WE CANNOT MOCK ANNOTATIONS
    public void testCreateImageWordsForLabelAnnotations() throws Exception {
        GoogleRetreiver googleRetreiver = new GoogleRetreiver();
        
        WordRepository wordRepository = mock(WordRepository.class);
        ImageRepository imageRepository = mock(ImageRepository.class);
        Image dbImage = mock(Image.class);
        AnnotateImageResponse response = mock(AnnotateImageResponse.class);
        WordCount dbWord = mock(WordCount.class);
        EntityAnnotation annotation = mock(EntityAnnotation.class);
        
        googleRetreiver.wordRepository = wordRepository;
        googleRetreiver.imageRepository = imageRepository;
        
        int imageId = new Random().nextInt();
        int wordId = new Random().nextInt();
        String categoryName = UUID.randomUUID().toString();
        String annotationText = UUID.randomUUID().toString();
        
        float confidence = 0.12345f;
        int expectedConfidence = 12;
        
        when(response.getLabelAnnotationsList()).thenReturn(Arrays.asList(annotation));
        when(response.hasError()).thenReturn(false);
        
        when(dbImage.getId()).thenReturn(imageId);
        when(dbImage.getCategory()).thenReturn(categoryName);
        when(dbWord.getId()).thenReturn(wordId);
        
        when(annotation.getDescription()).thenReturn(annotationText);
        when(annotation.getConfidence()).thenReturn(confidence);
        
        when(wordRepository.getWordByText(eq(annotationText), eq(categoryName))).thenReturn(dbWord);
        
        googleRetreiver.createImageWordsForLabelAnnotations(dbImage, Arrays.asList(response));
        
        verify(wordRepository).getWordByText(eq(annotationText), eq(categoryName));
        verifyNoMoreInteractions(wordRepository);
        
        verify(imageRepository).addWordToImage(eq(imageId), eq(wordId), eq(expectedConfidence));
        verifyNoMoreInteractions(imageRepository);
    }


    protected GoogleImage convertImage(File imageFile) throws Exception {
        Assert.assertTrue(imageFile.isFile());
        ImageConverter imageConverter = new ImageConverter();
        Field f1 = imageConverter.getClass().getDeclaredField("conf");
        f1.setAccessible(true);
        f1.set(imageConverter, conf);

        File jpegFile = imageConverter.convertTiff(imageFile);

        return new GoogleImage(jpegFile);
    }
}
