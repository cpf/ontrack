package net.ontrack.core.model;

import lombok.Data;
import net.ontrack.core.validation.NameDescription;

@Data
public class ValidationStampUpdateForm implements NameDescription {

	private final String name;
	private final String description;

}
