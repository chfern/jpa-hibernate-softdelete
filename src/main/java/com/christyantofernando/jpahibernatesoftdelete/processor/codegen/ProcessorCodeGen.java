package com.christyantofernando.jpahibernatesoftdelete.processor.codegen;


import com.christyantofernando.jpahibernatesoftdelete.processor.common.SoftDeleteAnnotatedModel;
import com.squareup.javapoet.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

public class ProcessorCodeGen {
    List<SoftDeleteAnnotatedModel> softDeleteAnnotatedModels;
    private static final String METHOD_NAME = "softDelete";
    private static final String REPO_PREFIX = "SoftDeleteRepository";

    public ProcessorCodeGen(final List<SoftDeleteAnnotatedModel> softDeleteAnnotatedModels){
        this.softDeleteAnnotatedModels = softDeleteAnnotatedModels;
    }

    public void generate(final Elements elementUtils, final Filer filer) throws IOException {
        for(SoftDeleteAnnotatedModel softDeleteAnnotatedModel : softDeleteAnnotatedModels){
            TypeElement typeElement = softDeleteAnnotatedModel.getTypeElement();
            AnnotationSpec softDeleteAnnotationSpec = AnnotationSpec.builder(Query.class)
                    .addMember("value", "$S",String.format("UPDATE %s SET %s = CURRENT_DATE WHERE %s = :id",
                            softDeleteAnnotatedModel.getCanonicalModelName(),
                            softDeleteAnnotatedModel.getSoftDeletePropertyName(),
                            softDeleteAnnotatedModel.getSoftDeleteIdPropertyName()))
                    .build();
            MethodSpec softDeleteSpec = MethodSpec.methodBuilder(METHOD_NAME)
                    .returns(int.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(Long.class, "id")
                    .addAnnotation(Transactional.class)
                    .addAnnotation(Modifying.class)
                    .addAnnotation(softDeleteAnnotationSpec)
                    .build();
            TypeSpec typeSpec = TypeSpec.interfaceBuilder(typeElement.getSimpleName().toString() + REPO_PREFIX)
                    .addJavadoc(String.format("Repository for soft delete operations on {@link %s}", softDeleteAnnotatedModel.getCanonicalModelName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get(CrudRepository.class),
                            ClassName.get(softDeleteAnnotatedModel.getModelPackage(), softDeleteAnnotatedModel.getSimpleModelName()),
                            ClassName.get(Long.class)
                    ))
                    .addMethod(softDeleteSpec)
                    .build();
            JavaFile javaFile = JavaFile.builder(elementUtils.getPackageOf(typeElement).getQualifiedName().toString(), typeSpec)
                    .build();
            javaFile.writeTo(filer);
        }
    }
}
