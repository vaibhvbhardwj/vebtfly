/**
 * OrganizerDashboard Component Tests
 * 
 * This file documents the test cases for the OrganizerDashboard component.
 * To run these tests, install a testing framework like Vitest or Jest.
 * 
 * Test Framework: Vitest (recommended) or Jest
 * Testing Library: React Testing Library
 * 
 * Installation:
 * npm install -D vitest @testing-library/react @testing-library/jest-dom
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import OrganizerDashboard from './OrganizerDashboard';

// Mock the useAuth hook
vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    user: {
      id: 1,
      name: 'Jane Smith',
      organizationName: 'Tech Events Inc',
      role: 'ORGANIZER',
      email: 'jane@example.com',
    },
    isAuthenticated: true,
    token: 'mock-token',
  }),
}));

describe('OrganizerDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Stat Calculations', () => {
    it('should display correct active events count', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const activeEventsElement = screen.getByText('Active Events').closest('div');
        expect(activeEventsElement).toBeInTheDocument();
        expect(activeEventsElement.textContent).toContain('3');
      });
    });

    it('should display correct pending applications count', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const pendingElement = screen.getByText('Pending Applications').closest('div');
        expect(pendingElement).toBeInTheDocument();
        expect(pendingElement.textContent).toContain('5');
      });
    });

    it('should display total volunteers hired correctly', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const volunteersElement = screen.getByText('Volunteers Hired').closest('div');
        expect(volunteersElement).toBeInTheDocument();
        expect(volunteersElement.textContent).toContain('24');
      });
    });

    it('should display average rating correctly', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const ratingElement = screen.getByText('Your Rating').closest('div');
        expect(ratingElement).toBeInTheDocument();
        expect(ratingElement.textContent).toContain('4.7');
      });
    });
  });

  describe('Quick Action Links', () => {
    it('should render Create Event link', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const createLink = screen.getByText('Create Event');
        expect(createLink).toBeInTheDocument();
        expect(createLink.closest('a')).toHaveAttribute('href', '/events/create');
      });
    });

    it('should render View Applications link', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const applicationsLink = screen.getByText('View Applications');
        expect(applicationsLink).toBeInTheDocument();
        expect(applicationsLink.closest('a')).toHaveAttribute('href', '/events/my-events');
      });
    });

    it('should render View All link for Upcoming Events', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const viewAllLinks = screen.getAllByText(/View All/);
        expect(viewAllLinks.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Dashboard Sections', () => {
    it('should display welcome message with organization name', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText(/Welcome back, Tech Events Inc/)).toBeInTheDocument();
      });
    });

    it('should display Upcoming Events section', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText(/Upcoming Events/)).toBeInTheDocument();
      });
    });

    it('should display My Events Summary', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('My Events Summary')).toBeInTheDocument();
      });
    });

    it('should display Pending Actions section', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText(/Pending Actions/)).toBeInTheDocument();
      });
    });

    it('should display Revenue & Volunteers Trend chart', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Revenue & Volunteers Trend')).toBeInTheDocument();
      });
    });

    it('should display Event Status pie chart', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Event Status')).toBeInTheDocument();
      });
    });
  });

  describe('Event Status Display', () => {
    it('should display event status breakdown', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const summarySection = screen.getByText('My Events Summary').closest('div');
        expect(summarySection).toBeInTheDocument();
        // Should show status counts
        expect(summarySection.textContent).toContain('PUBLISHED');
        expect(summarySection.textContent).toContain('DEPOSIT_PAID');
        expect(summarySection.textContent).toContain('COMPLETED');
      });
    });
  });

  describe('Volunteer Progress Tracking', () => {
    it('should display volunteer confirmation progress for events', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const upcomingSection = screen.getByText(/Upcoming Events/).closest('div');
        expect(upcomingSection).toBeInTheDocument();
        // Should show volunteer counts
        expect(upcomingSection.textContent).toContain('Volunteers');
      });
    });
  });

  describe('Loading State', () => {
    it('should display loading spinner while fetching data', () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      // The loading state should be brief, but we can verify the component renders
      expect(screen.getByText(/Loading your dashboard/i)).toBeInTheDocument();
    });
  });

  describe('Responsive Design', () => {
    it('should render stats grid with responsive columns', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const statsGrid = screen.getByText('Active Events').closest('div').closest('div').closest('div');
        expect(statsGrid).toHaveClass('grid');
        expect(statsGrid).toHaveClass('grid-cols-1');
        expect(statsGrid).toHaveClass('md:grid-cols-2');
        expect(statsGrid).toHaveClass('lg:grid-cols-4');
      });
    });
  });

  describe('Pending Actions Display', () => {
    it('should display all pending actions', async () => {
      render(
        <BrowserRouter>
          <OrganizerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const pendingSection = screen.getByText(/Pending Actions/).closest('div');
        expect(pendingSection).toBeInTheDocument();
        // Should show action items
        expect(pendingSection.textContent).toContain('applications');
        expect(pendingSection.textContent).toContain('deposit');
        expect(pendingSection.textContent).toContain('attendance');
      });
    });
  });
});
