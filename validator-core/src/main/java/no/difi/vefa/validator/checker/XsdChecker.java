package no.difi.vefa.validator.checker;

import no.difi.vefa.validator.api.Checker;
import no.difi.vefa.validator.api.CheckerInfo;
import no.difi.vefa.validator.Document;
import no.difi.vefa.validator.Configuration;
import no.difi.vefa.validator.util.PathLSResolveResource;
import no.difi.vefa.validator.Section;
import no.difi.xsd.vefa.validator._1.FlagType;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.nio.file.Files;
import java.nio.file.Path;

@CheckerInfo({".xsd"})
public class XsdChecker implements Checker {

    private Validator validator;

    public XsdChecker(Path path) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setResourceResolver(new PathLSResolveResource(path.getParent()));
            Schema schema = schemaFactory.newSchema(new StreamSource(Files.newInputStream(path)));
            validator = schema.newValidator();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Section check(Document document, Configuration configuration) throws Exception {
        Source xmlFile = new StreamSource(document.getInputStream());
        Section section = new Section(document, configuration);
        section.setTitle("XSD validation");
        section.setFlag(FlagType.OK);

        long tsStart = System.currentTimeMillis();
        try {
            validator.validate(xmlFile);
            section.setRuntime((System.currentTimeMillis() - tsStart) + "ms");
        } catch (SAXException e) {
            section.setRuntime((System.currentTimeMillis() - tsStart) + "ms");
            section.add("XSD", e.getLocalizedMessage(), FlagType.FATAL);
        }

        return section;
    }
}