/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.valueextraction.Unwrapping;
import javax.validation.valueextraction.ValueExtractor;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

/**
 * Tests for JavaFX {@link ValueExtractor}s.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
public class JavaFXValueExtractorsTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void testJavaFXPropertyDefaultUnwrapping() {
		Set<ConstraintViolation<BasicPropertiesEntity>> constraintViolations = validator.validate( new BasicPropertiesEntity() );
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"doubleProperty",
				"integerProperty",
				"booleanProperty" );
		assertCorrectConstraintTypes(
				constraintViolations,
				Max.class,
				Min.class,
				AssertTrue.class );
	}

	@Test
	public void testValueExtractionForPropertyList() {
		Set<ConstraintViolation<ListPropertyEntity>> constraintViolations = validator.validate( ListPropertyEntity.valid() );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( ListPropertyEntity.invalidList() );
		assertCorrectPropertyPaths( constraintViolations, "listProperty" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );

		constraintViolations = validator.validate( ListPropertyEntity.invalidListElement() );
		assertCorrectPropertyPaths( constraintViolations, "listProperty[0].<list element>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
	}

	@Test
	public void testValueExtractionForPropertySet() {
		Set<ConstraintViolation<SetPropertyEntity>> constraintViolations = validator.validate( SetPropertyEntity.valid() );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( SetPropertyEntity.invalidSet() );
		assertCorrectPropertyPaths( constraintViolations, "setProperty" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );

		constraintViolations = validator.validate( SetPropertyEntity.invalidSetElement() );
		assertCorrectPropertyPaths( constraintViolations, "setProperty[].<iterable element>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );
	}

	@Test
	public void testValueExtractionForPropertyMap() {
		Set<ConstraintViolation<MapPropertyEntity>> constraintViolations = validator.validate( MapPropertyEntity.valid() );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( MapPropertyEntity.invalidMap() );
		assertCorrectPropertyPaths( constraintViolations, "mapProperty" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );

		constraintViolations = validator.validate( MapPropertyEntity.invalidMapKey() );
		assertCorrectPropertyPaths( constraintViolations, "mapProperty<K>[app].<map key>" );
		assertCorrectConstraintTypes( constraintViolations, Size.class );

		constraintViolations = validator.validate( MapPropertyEntity.invalidMapValue() );
		assertCorrectPropertyPaths( constraintViolations, "mapProperty[pear].<map value>" );
		assertCorrectConstraintTypes( constraintViolations, Email.class );
	}

	@Test
	public void testJavaFXPropertySkipUnwrapping() {
		Set<ConstraintViolation<DefaultUnwrappingEntity>> constraintViolationsDefault = validator.validate( new DefaultUnwrappingEntity() );
		assertNumberOfViolations( constraintViolationsDefault, 1 );
		assertCorrectPropertyPaths( constraintViolationsDefault, "property" );
		assertCorrectConstraintTypes( constraintViolationsDefault, NotNull.class );

		Set<ConstraintViolation<SkipUnwrappingEntity>> constraintViolationsSkip = validator.validate( new SkipUnwrappingEntity() );
		assertNumberOfViolations( constraintViolationsSkip, 0 );
	}

	public class BasicPropertiesEntity {

		@Max(value = 3)
		private ReadOnlyDoubleWrapper doubleProperty = new ReadOnlyDoubleWrapper( 4.5 );

		@Min(value = 3)
		private IntegerProperty integerProperty = new SimpleIntegerProperty( 2 );

		@AssertTrue
		private ReadOnlyBooleanProperty booleanProperty = new SimpleBooleanProperty( false );
	}

	public static class ListPropertyEntity {

		@Size(min = 3)
		private ListProperty<@Size(min = 4) String> listProperty;

		private ListPropertyEntity(ObservableList<String> innerList) {
			this.listProperty = new ReadOnlyListWrapper<String>( innerList );
		}

		public static ListPropertyEntity valid() {
			return new ListPropertyEntity( FXCollections.observableArrayList( "apple", "pear", "cherry" ) );
		}

		public static ListPropertyEntity invalidList() {
			return new ListPropertyEntity( FXCollections.observableArrayList( "apple" ) );
		}

		public static ListPropertyEntity invalidListElement() {
			return new ListPropertyEntity( FXCollections.observableArrayList( "app", "pear", "cherry" ) );
		}
	}

	public static class SetPropertyEntity {

		@Size(min = 3)
		private SetProperty<@Size(min = 4) String> setProperty;

		private SetPropertyEntity(ObservableSet<String> innerList) {
			this.setProperty = new ReadOnlySetWrapper<String>( innerList );
		}

		public static SetPropertyEntity valid() {
			return new SetPropertyEntity( FXCollections.observableSet( "apple", "pear", "cherry" ) );
		}

		public static SetPropertyEntity invalidSet() {
			return new SetPropertyEntity( FXCollections.observableSet( "apple" ) );
		}

		public static SetPropertyEntity invalidSetElement() {
			return new SetPropertyEntity( FXCollections.observableSet( "app", "pear", "cherry" ) );
		}
	}

	public static class MapPropertyEntity {

		@Size(min = 3)
		private MapProperty<@Size(min = 4) String, @Email String> mapProperty = new ReadOnlyMapWrapper<String, String>();

		private MapPropertyEntity(ObservableMap<String, String> innerMap) {
			this.mapProperty = new ReadOnlyMapWrapper<String, String>( innerMap );
		}

		public static MapPropertyEntity valid() {
			ObservableMap<String, String> innerMap = FXCollections.observableHashMap();
			innerMap.put( "apple", "apple@example.com" );
			innerMap.put( "pear", "pear@example.com" );
			innerMap.put( "cherry", "cherry@example.com" );

			return new MapPropertyEntity( innerMap );
		}

		public static MapPropertyEntity invalidMap() {
			return new MapPropertyEntity( FXCollections.observableHashMap() );
		}

		public static MapPropertyEntity invalidMapKey() {
			ObservableMap<String, String> innerMap = FXCollections.observableHashMap();
			innerMap.put( "app", "apple@example.com" );
			innerMap.put( "pear", "pear@example.com" );
			innerMap.put( "cherry", "cherry@example.com" );

			return new MapPropertyEntity( innerMap );
		}

		public static MapPropertyEntity invalidMapValue() {
			ObservableMap<String, String> innerMap = FXCollections.observableHashMap();
			innerMap.put( "apple", "apple@example.com" );
			innerMap.put( "pear", "pear" );
			innerMap.put( "cherry", "cherry@example.com" );

			return new MapPropertyEntity( innerMap );
		}
	}

	public class DefaultUnwrappingEntity {

		@NotNull
		private StringProperty property = new SimpleStringProperty( null );
	}

	public class SkipUnwrappingEntity {

		@NotNull(payload = { Unwrapping.Skip.class })
		private StringProperty property = new SimpleStringProperty( null );
	}
}
