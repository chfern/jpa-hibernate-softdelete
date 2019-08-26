# jpa-hibernate-softdelete

## What is Soft Delete

To mark a record in a database for deletion or to temporarily prevent it from being selected  

## Achieving Soft Delete with JPA & Hibernate
With the help of jpa-hibernate-softdelete annotation processor, implementing soft deletion is pretty much straight forward.
The package consists of 2 annotations:
- @SoftDelete  
  Used to annotate model classes that we want to apply soft delete to.
- @DeletedAt  
  Determines which property (Date) to be used for soft deleting.

The class must also has one primary key property, annotated with @Id (from javax.persistence) with Long datatype.

### Example
Let's say we have a model class named ```Todo```  
```
@Entity
@Table(name = "todos")
@SoftDelete // Enables soft deletion on model
@Where(clause = "deleted_at IS NULL")
public class Todo {
    @Id // Every soft delete model needs to have a Long @Id property
    @GeneratedValue
    @Column (name = "my_todo_id")
    private Long id;

    @Column (name = "deleted_at")
    @DeletedAt // This property will be used for soft deletion
    @Nullable
    private Date deletedAt = null; 

    Todo(){
        id = null;
        name = null;
    }
}

```
  
Now, try to re-build the project, and a new generated class with format *modelname*SoftDeleteRepository will be created,
and also a function softDelete(Long id) to actually perform the soft delete.

This new repository can be combined with your DI provider, e.g: AutoWire
```
todoSoftDeleteRepository.softDelete(1); // Soft deleting todo where id = 1
```

## Using this plugin
This plugin is available in maven
```
<dependency>
  <groupId>com.christyantofernando</groupId>
  <artifactId>jpa-hibernate-softdelete</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```