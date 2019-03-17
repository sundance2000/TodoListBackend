package io.swagger.model;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Objects;

public class OffsetPageRequest extends PageRequest {

    private static final long serialVersionUID = 2909051847053136720L;

    private int offset;

    public OffsetPageRequest(int page, int size, int offset) {
        super(page, size);
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
