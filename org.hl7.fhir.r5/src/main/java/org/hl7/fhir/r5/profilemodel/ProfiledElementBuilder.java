package org.hl7.fhir.r5.profilemodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.r5.conformance.ProfileUtilities;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.ElementDefinition;
import org.hl7.fhir.r5.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.ResourceFactory;
import org.hl7.fhir.r5.model.StructureDefinition;

public class ProfiledElementBuilder {

  private IWorkerContext context;
  private ProfileUtilities pu;

  public ProfiledElementBuilder(IWorkerContext context) {
    super();
    this.context = context;
    pu = new ProfileUtilities(context, null, null);
  }
  

  /**
   * Given a profile, return a tree of elements in the profile model. This builds the profile model
   * for the latest version of the nominated profile
   * 
   * THe tree of elements in the profile model is different to the the base resource:
   *  - some elements are removed (max = 0)
   *  - extensions are turned into named elements 
   *  - slices are turned into named elements 
   *  - element properties - doco, cardinality, binding etc is updated for what the profile says
   * 
   * When built with this method, the profile element can't have instance data
   * 
   * Warning: profiles and resources can be recursive; you can't iterate this tree until it you get 
   * to the leaves because you will never get to a child that doesn't have children
   * 
   */
  public ProfiledElement buildProfileElement(String url) {
    StructureDefinition profile = getProfile(url);
    if (profile == null) {
      throw new DefinitionException("Unable to find profile for URL '"+url+"'");
    }
    StructureDefinition base = context.fetchTypeDefinition(profile.getType());
    if (base == null) {
      throw new DefinitionException("Unable to find base type '"+profile.getType()+"' for URL '"+url+"'");
    }
    return new ProfiledElement(this, profile.getName(), base, base.getSnapshot().getElementFirstRep(), profile, profile.getSnapshot().getElementFirstRep());
  }
  
  /**
   * Given a profile, return a tree of elements in the profile model. This builds the profile model
   * for the nominated version of the nominated profile
   * 
   * THe tree of elements in the profile model is different to the the base resource:
   *  - some elements are removed (max = 0)
   *  - extensions are turned into named elements 
   *  - slices are turned into named elements 
   *  - element properties - doco, cardinality, binding etc is updated for what the profile says
   * 
   * When built with this method, the profile element can't have instance data
   * 
   * Warning: profiles and resources can be recursive; you can't iterate this tree until it you get 
   * to the leaves because you will never get to a child that doesn't have children
   * 
   */
  public ProfiledElement buildProfileElement(String url, String version) {
    StructureDefinition profile = getProfile(url, version);
    if (profile == null) {
      throw new DefinitionException("Unable to find profile for URL '"+url+"'");
    }
    StructureDefinition base = context.fetchTypeDefinition(profile.getType());
    if (base == null) {
      throw new DefinitionException("Unable to find base type '"+profile.getType()+"' for URL '"+url+"'");
    }
    return new ProfiledElement(this, profile.getName(), base, base.getSnapshot().getElementFirstRep(), profile, profile.getSnapshot().getElementFirstRep());
  }
  
  /**
   * Given a profile, return a tree of elements in the profile model with matching instance data. 
   * This builds the profile model for the latest version of the nominated profile and matches 
   * the data in the resource against the profile. Data can be added or read from the profile element
   * 
   * THe tree of elements in the profile model is different to the the base resource:
   *  - some elements are removed (max = 0)
   *  - extensions are turned into named elements 
   *  - slices are turned into named elements 
   *  - element properties - doco, cardinality, binding etc is updated for what the profile says
   * 
   * When built with this method, the profile element can't have instance data
   * 
   * Warning: profiles and resources can be recursive; you can't iterate this tree until it you get 
   * to the leaves because you will never get to a child that doesn't have children
   * 
   */
  public ProfiledElement buildProfileElement(String url, Resource resource) {
    throw new NotImplementedException("NOt done yet");
  }
  
  /**
   * Given a profile, return a tree of elements in the profile model with matching instance data. 
   * This builds the profile model for the nominated version of the nominated profile and matches 
   * the data in the resource against the profile. Data can be added or read from the profile element
   * 
   * THe tree of elements in the profile model is different to the the base resource:
   *  - some elements are removed (max = 0)
   *  - extensions are turned into named elements 
   *  - slices are turned into named elements 
   *  - element properties - doco, cardinality, binding etc is updated for what the profile says
   * 
   * When built with this method, the profile element can't have instance data
   * 
   */
  public ProfiledElement buildProfileElement(String url, String version, Resource resource) {
    throw new NotImplementedException("NOt done yet");
  }
  
  /**
   * Given a profile, construct an empty resource of the type being profiled (to use as input 
   * to the buildProfileElement method
   * 
   * No version, because the version doesn't change the type of the resource
   */
  public Resource makeProfileBase(String url) {
    StructureDefinition profile = getProfile(url);
    return ResourceFactory.createResource(profile.getType());
  }


  // -- methods below here are only used internally to the package

  private StructureDefinition getProfile(String url) {
    return context.fetchResource(StructureDefinition.class, url);
  }


  private StructureDefinition getProfile(String url, String version) {
    return context.fetchResource(StructureDefinition.class, url, version);
  }

  protected List<ProfiledElement> listChildren(StructureDefinition baseStructure, ElementDefinition baseDefinition,
      StructureDefinition profileStructure, ElementDefinition profiledDefinition, TypeRefComponent t, CanonicalType u) {
    // TODO Auto-generated method stub
    return null;
  }


  protected List<ProfiledElement> listChildren(StructureDefinition baseStructure, ElementDefinition baseDefinition, StructureDefinition profileStructure, ElementDefinition profileDefinition, TypeRefComponent t) {
    if (profileDefinition.getType().size() == 1 || (!profileDefinition.getPath().contains("."))) {
      assert profileDefinition.getType().size() != 1 || profileDefinition.getType().contains(t);
      List<ElementDefinition> list = pu.getChildList(profileStructure, profileDefinition);
      if (list != null && list.size() > 0) {
        List<ElementDefinition> blist = pu.getChildList(baseStructure, baseDefinition);
        List<ProfiledElement> res = new ArrayList<>();
        int i = 0;
        while (i < list.size()) {
          ElementDefinition defn = list.get(i);
          if (defn.hasSlicing()) {
            i++;
            while (i < list.size() && list.get(i).getPath().equals(defn.getPath())) {
              res.add(new ProfiledElement(this, list.get(i).getSliceName(), baseStructure, getByName(blist, defn), profileStructure, list.get(i), defn));
              i++;
            }
          } else {
            res.add(new ProfiledElement(this, defn.getName(), baseStructure, getByName(blist, defn), profileStructure, defn));
            i++;
          }
        }
        return res;
      } else {
        throw new DefinitionException("not done yet");
      }
    } else {
      throw new DefinitionException("not done yet");
    }
  }


  private ElementDefinition getByName(List<ElementDefinition> blist, ElementDefinition defn) {
    for (ElementDefinition ed : blist) {
      if (ed.getPath().equals(defn.getPath())) {
        return ed;
      }
    }
    return null;
  }
 
}
