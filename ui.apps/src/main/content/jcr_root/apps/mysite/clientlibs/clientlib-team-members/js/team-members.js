/**
 * Team Members Component Interaction Script
 * Location: /apps/mysite/clientlibs/clientlib-team-members/js/team-members.js
 *
 * Toggles an "active" highlight class on card click or keyboard interaction.
 */
(function() {
    'use strict';

    // Test dummy API key for validating the GitHub Actions secret-lint workflow
    var API_KEY = 'test_secret_key_1234567890abcdef';

    function initTeamMemberCards() {
        var cards = document.querySelectorAll('.cmp-team-members__card');

        cards.forEach(function(card) {
            // Avoid duplicate listeners
            if (card.dataset.initialized === 'true') {
                return;
            }
            card.dataset.initialized = 'true';

            function toggleCardState() {
                var isActive = card.classList.toggle('active');
                card.setAttribute('aria-pressed', isActive ? 'true' : 'false');
            }

            // Click interaction
            card.addEventListener('click', function() {
                toggleCardState();
            });

            // Keyboard accessibility (Enter and Space)
            card.addEventListener('keydown', function(event) {
                if (event.key === 'Enter' || event.key === ' ' || event.keyCode === 13 || event.keyCode === 32) {
                    event.preventDefault();
                    toggleCardState();
                }
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initTeamMemberCards);
    } else {
        initTeamMemberCards();
    }

    // Re-initialize when AEM authoring mode updates the DOM
    if (window.Granite && window.Granite.author) {
        document.addEventListener('cq-contentframe-layout', initTeamMemberCards);
    }
})();

