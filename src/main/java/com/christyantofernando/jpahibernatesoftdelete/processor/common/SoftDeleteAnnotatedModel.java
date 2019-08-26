package com.christyantofernando.jpahibernatesoftdelete.processor.common;

import com.christyantofernando.jpahibernatesoftdelete.annotation.DeletedAt;
import com.christyantofernando.jpahibernatesoftdelete.annotation.SoftDelete;
import org.springframework.data.repository.CrudRepository;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.persistence.Id;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.List;

public class SoftDeleteAnnotatedModel {
    private TypeElement typeElement;
    private String softDeletePropertyName;
    private String softDeleteIdPropertyName;

    private String canonicalModelName;
    private String simpleModelName;
    private String modelPackage;
    private Types typeUtils;

    public SoftDeleteAnnotatedModel(final Types typeUtils) {
        this.typeUtils = typeUtils;
    }

    public SoftDeleteAnnotatedModel tryInitialize(final TypeElement typeElement) throws Exception {
        String softDeletePropertyName = tryGetSoftDeletePropertyName(typeElement);
        if (softDeletePropertyName == null)
            throw new Exception(String.format("No Date field annotated with @%s found. Make sure you have one Date field annotated with @%s in your model class ",
                    SoftDelete.class.getSimpleName(), SoftDelete.class.getSimpleName()));

        this.softDeletePropertyName = softDeletePropertyName;

        this.canonicalModelName = typeElement.getQualifiedName().toString();
        this.simpleModelName = typeElement.getSimpleName().toString();
        String modelPackage = this.canonicalModelName.substring(0, this.canonicalModelName.length() - this.simpleModelName.length());
        if(modelPackage.endsWith(".")) modelPackage = modelPackage.substring(0, modelPackage.length() - 1);
        this.modelPackage = modelPackage;

        String softDeleteIdPropertyname = tryGetSoftDeleteIdPropertyName(typeElement);
        if (softDeleteIdPropertyname == null)
            throw new Exception(String.format("No field with @%s found.",
                    Id.class.getSimpleName()));
        this.softDeleteIdPropertyName = softDeleteIdPropertyname;

        this.typeElement = typeElement;

        return this;
    }

    private void checkAnnotatedElementIsSubclassOfCrudRepository(TypeElement currentClass) throws Exception {
        while (true) {
            TypeMirror superClassType = currentClass.getSuperclass();
            if (superClassType.getKind() == TypeKind.NONE) {
                // Basis class (java.lang.Object) reached, so exit
                throw new Exception(String.format("The interface %s annotated with @%s must inherit from %s",
                        currentClass.getSimpleName().toString(), DeletedAt.class.getSimpleName(),
                        CrudRepository.class.getCanonicalName()));
            }
            if (superClassType.toString().equals(CrudRepository.class.getCanonicalName())) {
                // Required super class found
                break;
            }
            // Moving up in inheritance tree
            currentClass = (TypeElement) typeUtils.asElement(superClassType);
        }
    }

    private String tryGetSoftDeletePropertyName(final TypeElement typeElement) {
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        for (VariableElement field : fields) {
            Annotation annotation = field.getAnnotation(DeletedAt.class);
            if (annotation == null) continue;

            TypeMirror fieldType = field.asType();
            String canonicalFieldType = fieldType.toString();
            if (!Date.class.getName().equals(canonicalFieldType)) continue;

            return field.getSimpleName().toString();
        }
        return null;
    }

    private String tryGetModelName(final TypeElement typeElement) {
        try {
            Class<?> clazz = null;
            return clazz.getCanonicalName();
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            return classTypeElement.getQualifiedName().toString();
        }
    }

    private String tryGetSoftDeleteIdPropertyName(final TypeElement typeElement) {
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        for (VariableElement field : fields) {
            Annotation annotation = field.getAnnotation(Id.class);
            if (annotation == null) continue;
            return field.getSimpleName().toString();
        }
        return null;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getSoftDeletePropertyName() {
        return softDeletePropertyName;
    }

    public String getCanonicalModelName() {
        return canonicalModelName;
    }
    public String getSimpleModelName(){ return simpleModelName; }
    public String getModelPackage(){return modelPackage; }

    public String getSoftDeleteIdPropertyName() {
        return softDeleteIdPropertyName;
    }
}

