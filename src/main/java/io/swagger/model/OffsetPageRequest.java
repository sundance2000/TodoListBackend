package io.swagger.model;

import org.springframework.data.domain.PageRequest;

import java.util.Objects;

public class OffsetPageRequest extends PageRequest {

    private static final long serialVersionUID = 2909051847053136720L;

    private final int offset;

    OffsetPageRequest(int limit, int offset) {
        super(0, limit);
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OffsetPageRequest that = (OffsetPageRequest) o;
        return offset == that.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), offset);
    }

}
