package navik.domain.position.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceType {

	ENTRY("신입"),
	EXPERIENCED("경력");

	private final String label;
}
