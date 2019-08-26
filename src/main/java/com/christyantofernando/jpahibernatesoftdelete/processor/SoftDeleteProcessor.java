package com.christyantofernando.jpahibernatesoftdelete.processor;

import com.christyantofernando.jpahibernatesoftdelete.annotation.SoftDelete;
import com.christyantofernando.jpahibernatesoftdelete.processor.codegen.ProcessorCodeGen;
import com.christyantofernando.jpahibernatesoftdelete.processor.common.SoftDeleteAnnotatedModel;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SoftDeleteProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        /**
         * Search for {@link SoftDelete} annotated interfaces
         */
        List<SoftDeleteAnnotatedModel> softDeleteAnnotatedModels = new ArrayList<SoftDeleteAnnotatedModel>();
        for(Element annotatedElement : roundEnv.getElementsAnnotatedWith(SoftDelete.class)){
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error("Only classes can be annotated with @%s", SoftDelete.class.getSimpleName());
                continue;
            }

            try {
                SoftDeleteAnnotatedModel softDeleteAnnotatedModel = new SoftDeleteAnnotatedModel(typeUtils);
                TypeElement interfaceAnnotatedTypeElement = (TypeElement) annotatedElement;
                softDeleteAnnotatedModel.tryInitialize(interfaceAnnotatedTypeElement);
                softDeleteAnnotatedModels.add(softDeleteAnnotatedModel);
            } catch (Exception e) {
                error(e.getMessage());
            }
        }

        /**
         * Code-gen
         */
        ProcessorCodeGen softDeleteAnnotated = new ProcessorCodeGen(softDeleteAnnotatedModels);
        try {
            softDeleteAnnotated.generate(elementUtils, filer);
        } catch (IOException e) {
            error(e.getMessage());
        } catch (Exception e) {
            error(e.getMessage());
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(SoftDelete.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args));
    }
}
