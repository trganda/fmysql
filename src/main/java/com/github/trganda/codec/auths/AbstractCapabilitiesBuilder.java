package com.github.trganda.codec.auths;

import com.github.trganda.codec.constants.CapabilityFlags;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

abstract class AbstractCapabilitiesBuilder<B extends AbstractCapabilitiesBuilder> {
  final Set<CapabilityFlags> capabilities = CapabilityFlags.getImplicitCapabilities();

  public B addCapabilities(CapabilityFlags... capabilities) {
    Collections.addAll(this.capabilities, capabilities);
    return (B) this;
  }

  public B addCapabilities(Collection<CapabilityFlags> capabilities) {
    this.capabilities.addAll(capabilities);
    return (B) this;
  }

  public boolean hasCapability(CapabilityFlags capability) {
    return capabilities.contains(capability);
  }
}
