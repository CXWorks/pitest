package org.pitest.mutationtest.execute;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.TimeoutLengthStrategy;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.config.MinionSettings;
import org.pitest.mutationtest.engine.EngineArguments;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.ExitCode;
import org.pitest.util.PitError;
import org.pitest.util.SafeDataInputStream;

public class MutationTestMinionTest {

  private MutationTestMinion           testee;

  @Mock
  private Reporter                    reporter;

  @Mock
  private SafeDataInputStream         is;

  @Mock
  private MutationEngine              engine;

  @Mock
  private TimeoutLengthStrategy       timeoutStrategy;

  @Mock
  private Mutater                     mutater;
  
  @Mock
  private MinionSettings              settings;

  private MinionArguments             args;

  private Collection<MutationDetails> mutations;

  private Collection<ClassName>       tests;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.mutations = new ArrayList<MutationDetails>();
    this.tests = new ArrayList<ClassName>();

    EngineArguments engineArguments = new EngineArguments("", Collections.<String>emptyList(), Collections.<String>emptyList());
    this.args = new MinionArguments(this.mutations, this.tests, engineArguments,
        this.timeoutStrategy, false, TestPluginArguments.defaults());

    when(this.is.readSerializable(MinionArguments.class)).thenReturn(this.args);
    when(this.engine.createMutator(any(ClassByteArraySource.class)))
    .thenReturn(this.mutater);
    
    MutationEngineFactory factory = Mockito.mock(MutationEngineFactory.class);
    when(factory.createEngine(any(EngineArguments.class))).thenReturn(engine);

    when(this.settings.createEngine(any(String.class))).thenReturn(factory);

    this.testee = new MutationTestMinion(settings, this.is, this.reporter);
  }

  @Test
  public void shouldReportNoErrorWhenNoMutationsSupplied() {
    this.testee.run();
    verify(this.reporter).done(ExitCode.OK);
  }

  @Test
  public void shouldReportErrorWhenOneOccursDuringAnalysis() {
    this.mutations.add(new MutationDetails(aMutationId().withIndex(0)
        .withMutator("foo").build(), "file", "desc", 0, 0));
    when(this.mutater.getMutation(any(MutationIdentifier.class))).thenThrow(
        new PitError("foo"));
    this.testee.run();
    verify(this.reporter).done(ExitCode.UNKNOWN_ERROR);
  }

}
